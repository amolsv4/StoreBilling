package com.retailpos.purchase.service;

import com.retailpos.common.exception.BusinessException;
import com.retailpos.common.exception.DuplicateResourceException;
import com.retailpos.common.exception.ResourceNotFoundException;
import com.retailpos.inventory.service.InventoryService;
import com.retailpos.inventory.service.InventoryTransactionType;
import com.retailpos.organization.entity.Organization;
import com.retailpos.organization.repository.OrganizationRepository;
import com.retailpos.product.entity.Product;
import com.retailpos.product.repository.ProductRepository;
import com.retailpos.purchase.dto.CreatePurchaseReceiptLineRequest;
import com.retailpos.purchase.dto.CreatePurchaseReceiptRequest;
import com.retailpos.purchase.dto.PurchaseReceiptLineResponse;
import com.retailpos.purchase.dto.PurchaseReceiptResponse;
import com.retailpos.purchase.dto.UpdatePurchaseReceiptLineRequest;
import com.retailpos.purchase.dto.UpdatePurchaseReceiptRequest;
import com.retailpos.purchase.entity.PurchaseReceipt;
import com.retailpos.purchase.entity.PurchaseReceiptLine;
import com.retailpos.purchase.entity.PurchaseReceiptStatus;
import com.retailpos.purchase.mapper.PurchaseReceiptLineMapper;
import com.retailpos.purchase.mapper.PurchaseReceiptMapper;
import com.retailpos.purchase.repository.PurchaseReceiptLineRepository;
import com.retailpos.purchase.repository.PurchaseReceiptRepository;
import com.retailpos.purchase.repository.PurchaseReceiptSpecifications;
import com.retailpos.store.entity.Store;
import com.retailpos.store.repository.StoreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Set;

@Service
public class PurchaseReceiptServiceImpl implements PurchaseReceiptService {

    private static final String REFERENCE_TYPE = "PURCHASE_RECEIPT";

    private final PurchaseReceiptRepository purchaseReceiptRepository;
    private final PurchaseReceiptLineRepository purchaseReceiptLineRepository;
    private final OrganizationRepository organizationRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final PurchaseReceiptMapper purchaseReceiptMapper;
    private final PurchaseReceiptLineMapper purchaseReceiptLineMapper;
    private final InventoryService inventoryService;

    public PurchaseReceiptServiceImpl(PurchaseReceiptRepository purchaseReceiptRepository,
                                       PurchaseReceiptLineRepository purchaseReceiptLineRepository,
                                       OrganizationRepository organizationRepository,
                                       StoreRepository storeRepository,
                                       ProductRepository productRepository,
                                       PurchaseReceiptMapper purchaseReceiptMapper,
                                       PurchaseReceiptLineMapper purchaseReceiptLineMapper,
                                       InventoryService inventoryService) {
        this.purchaseReceiptRepository = purchaseReceiptRepository;
        this.purchaseReceiptLineRepository = purchaseReceiptLineRepository;
        this.organizationRepository = organizationRepository;
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
        this.purchaseReceiptMapper = purchaseReceiptMapper;
        this.purchaseReceiptLineMapper = purchaseReceiptLineMapper;
        this.inventoryService = inventoryService;
    }

    @Override
    @Transactional
    public PurchaseReceiptResponse create(CreatePurchaseReceiptRequest request) {
        if (purchaseReceiptRepository.existsByOrganizationIdAndReceiptNumber(request.organizationId(), request.receiptNumber())) {
            throw new DuplicateResourceException(
                    "A purchase receipt with number '" + request.receiptNumber() + "' already exists for this organization.");
        }
        Organization organization = organizationRepository.findById(request.organizationId())
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Organization", request.organizationId()));
        Store store = requireStoreInOrganization(request.storeId(), request.organizationId());

        PurchaseReceipt receipt = purchaseReceiptMapper.toEntity(request);
        receipt.setOrganization(organization);
        receipt.setStore(store);
        receipt.setStatus(PurchaseReceiptStatus.DRAFT);
        receipt.setTotalAmount(BigDecimal.ZERO.setScale(2));
        PurchaseReceipt saved = purchaseReceiptRepository.save(receipt);
        return toResponse(saved, List.of());
    }

    @Override
    @Transactional
    public PurchaseReceiptResponse update(Long id, UpdatePurchaseReceiptRequest request) {
        PurchaseReceipt receipt = getEntity(id);
        assertMutable(receipt);
        if (!receipt.getReceiptNumber().equals(request.receiptNumber())
                && purchaseReceiptRepository.existsByOrganizationIdAndReceiptNumber(
                        receipt.getOrganization().getId(), request.receiptNumber())) {
            throw new DuplicateResourceException(
                    "A purchase receipt with number '" + request.receiptNumber() + "' already exists for this organization.");
        }
        purchaseReceiptMapper.updateEntityFromRequest(request, receipt);
        PurchaseReceipt saved = purchaseReceiptRepository.save(receipt);
        return toResponse(saved, currentLines(id));
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseReceiptResponse getById(Long id) {
        PurchaseReceipt receipt = getEntity(id);
        return toResponse(receipt, currentLines(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PurchaseReceiptResponse> list(Long organizationId, PurchaseReceiptStatus status,
                                               Set<Long> storeIdFilter, Pageable pageable) {
        return purchaseReceiptRepository
                .findAll(PurchaseReceiptSpecifications.filter(organizationId, status, storeIdFilter), pageable)
                .map(receipt -> toResponse(receipt, currentLines(receipt.getId())));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        PurchaseReceipt receipt = getEntity(id);
        assertMutable(receipt);
        purchaseReceiptLineRepository.deleteAllByPurchaseReceiptId(id);
        purchaseReceiptRepository.delete(receipt);
    }

    @Override
    @Transactional
    public PurchaseReceiptResponse addLine(Long id, CreatePurchaseReceiptLineRequest request) {
        PurchaseReceipt receipt = getEntity(id);
        assertMutable(receipt);
        Product product = requireProductInOrganization(request.productId(), receipt.getOrganization().getId());

        PurchaseReceiptLine line = new PurchaseReceiptLine();
        line.setPurchaseReceipt(receipt);
        line.setProduct(product);
        line.setQuantity(request.quantity());
        line.setUnitCost(request.unitCost());
        line.setLineTotal(lineTotal(request.quantity(), request.unitCost()));
        purchaseReceiptLineRepository.save(line);

        return toResponse(recomputeTotal(receipt), currentLines(id));
    }

    @Override
    @Transactional
    public PurchaseReceiptResponse updateLine(Long id, Long lineId, UpdatePurchaseReceiptLineRequest request) {
        PurchaseReceipt receipt = getEntity(id);
        assertMutable(receipt);
        PurchaseReceiptLine line = purchaseReceiptLineRepository.findByIdAndPurchaseReceiptId(lineId, id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("PurchaseReceiptLine", lineId));

        line.setQuantity(request.quantity());
        line.setUnitCost(request.unitCost());
        line.setLineTotal(lineTotal(request.quantity(), request.unitCost()));
        purchaseReceiptLineRepository.save(line);

        return toResponse(recomputeTotal(receipt), currentLines(id));
    }

    @Override
    @Transactional
    public PurchaseReceiptResponse removeLine(Long id, Long lineId) {
        PurchaseReceipt receipt = getEntity(id);
        assertMutable(receipt);
        PurchaseReceiptLine line = purchaseReceiptLineRepository.findByIdAndPurchaseReceiptId(lineId, id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("PurchaseReceiptLine", lineId));
        purchaseReceiptLineRepository.delete(line);

        return toResponse(recomputeTotal(receipt), currentLines(id));
    }

    @Override
    @Transactional
    public PurchaseReceiptResponse receive(Long id) {
        PurchaseReceipt receipt = getEntity(id);
        assertMutable(receipt);
        List<PurchaseReceiptLine> lines = purchaseReceiptLineRepository.findAllByPurchaseReceiptId(id);
        if (lines.isEmpty()) {
            throw new BusinessException("Cannot receive a purchase receipt with no lines.");
        }

        for (PurchaseReceiptLine line : lines) {
            inventoryService.applyStockChange(
                    line.getProduct().getId(),
                    receipt.getStore().getId(),
                    line.getQuantity(),
                    InventoryTransactionType.PURCHASE,
                    REFERENCE_TYPE,
                    receipt.getId(),
                    null);
        }

        receipt.setStatus(PurchaseReceiptStatus.RECEIVED);
        PurchaseReceipt saved = purchaseReceiptRepository.save(receipt);
        return toResponse(saved, lines);
    }

    private PurchaseReceipt recomputeTotal(PurchaseReceipt receipt) {
        BigDecimal total = purchaseReceiptLineRepository.findAllByPurchaseReceiptId(receipt.getId()).stream()
                .map(PurchaseReceiptLine::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        receipt.setTotalAmount(total);
        return purchaseReceiptRepository.save(receipt);
    }

    private static BigDecimal lineTotal(BigDecimal quantity, BigDecimal unitCost) {
        return quantity.multiply(unitCost).setScale(2, RoundingMode.HALF_UP);
    }

    private void assertMutable(PurchaseReceipt receipt) {
        if (receipt.getStatus() != PurchaseReceiptStatus.DRAFT) {
            throw new BusinessException("Purchase receipt " + receipt.getId() + " is " + receipt.getStatus()
                    + " and can no longer be edited - post a stock adjustment to correct it instead.");
        }
    }

    private Store requireStoreInOrganization(Long storeId, Long organizationId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Store", storeId));
        if (!store.getOrganization().getId().equals(organizationId)) {
            throw new BusinessException("Store " + storeId + " does not belong to organization " + organizationId + ".");
        }
        return store;
    }

    private Product requireProductInOrganization(Long productId, Long organizationId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Product", productId));
        if (!product.getOrganization().getId().equals(organizationId)) {
            throw new BusinessException("Product " + productId + " does not belong to organization " + organizationId + ".");
        }
        if (!product.isPurchaseEnabled()) {
            throw new BusinessException("Product " + productId + " is not enabled for purchasing.");
        }
        return product;
    }

    private List<PurchaseReceiptLine> currentLines(Long receiptId) {
        return purchaseReceiptLineRepository.findAllByPurchaseReceiptId(receiptId);
    }

    private List<PurchaseReceiptLineResponse> mapLines(List<PurchaseReceiptLine> lines) {
        return lines.stream().map(purchaseReceiptLineMapper::toResponse).toList();
    }

    private PurchaseReceiptResponse toResponse(PurchaseReceipt receipt, List<PurchaseReceiptLine> lines) {
        return new PurchaseReceiptResponse(
                receipt.getId(),
                receipt.getOrganization().getId(),
                receipt.getStore().getId(),
                receipt.getStore().getCode(),
                receipt.getStore().getName(),
                receipt.getSupplierName(),
                receipt.getReceiptNumber(),
                receipt.getReceiptDate(),
                receipt.getStatus(),
                receipt.getTotalAmount(),
                mapLines(lines),
                receipt.getCreatedDate(),
                receipt.getUpdatedDate());
    }

    private PurchaseReceipt getEntity(Long id) {
        return purchaseReceiptRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("PurchaseReceipt", id));
    }
}
