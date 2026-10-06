package com.retailpos.stocktransfer.service;

import com.retailpos.common.exception.BusinessException;
import com.retailpos.common.exception.DuplicateResourceException;
import com.retailpos.common.exception.ResourceNotFoundException;
import com.retailpos.inventory.service.InventoryService;
import com.retailpos.inventory.service.InventoryTransactionType;
import com.retailpos.organization.entity.Organization;
import com.retailpos.organization.repository.OrganizationRepository;
import com.retailpos.product.entity.Product;
import com.retailpos.product.repository.ProductRepository;
import com.retailpos.stocktransfer.dto.CreateStockTransferLineRequest;
import com.retailpos.stocktransfer.dto.CreateStockTransferRequest;
import com.retailpos.stocktransfer.dto.StockTransferLineResponse;
import com.retailpos.stocktransfer.dto.StockTransferResponse;
import com.retailpos.stocktransfer.dto.UpdateStockTransferLineRequest;
import com.retailpos.stocktransfer.dto.UpdateStockTransferRequest;
import com.retailpos.stocktransfer.entity.StockTransfer;
import com.retailpos.stocktransfer.entity.StockTransferLine;
import com.retailpos.stocktransfer.entity.StockTransferStatus;
import com.retailpos.stocktransfer.mapper.StockTransferLineMapper;
import com.retailpos.stocktransfer.mapper.StockTransferMapper;
import com.retailpos.stocktransfer.repository.StockTransferLineRepository;
import com.retailpos.stocktransfer.repository.StockTransferRepository;
import com.retailpos.stocktransfer.repository.StockTransferSpecifications;
import com.retailpos.store.entity.Store;
import com.retailpos.store.repository.StoreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class StockTransferServiceImpl implements StockTransferService {

    private static final String REFERENCE_TYPE = "STOCK_TRANSFER";

    private final StockTransferRepository stockTransferRepository;
    private final StockTransferLineRepository stockTransferLineRepository;
    private final OrganizationRepository organizationRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final StockTransferMapper stockTransferMapper;
    private final StockTransferLineMapper stockTransferLineMapper;
    private final InventoryService inventoryService;

    public StockTransferServiceImpl(StockTransferRepository stockTransferRepository,
                                     StockTransferLineRepository stockTransferLineRepository,
                                     OrganizationRepository organizationRepository,
                                     StoreRepository storeRepository,
                                     ProductRepository productRepository,
                                     StockTransferMapper stockTransferMapper,
                                     StockTransferLineMapper stockTransferLineMapper,
                                     InventoryService inventoryService) {
        this.stockTransferRepository = stockTransferRepository;
        this.stockTransferLineRepository = stockTransferLineRepository;
        this.organizationRepository = organizationRepository;
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
        this.stockTransferMapper = stockTransferMapper;
        this.stockTransferLineMapper = stockTransferLineMapper;
        this.inventoryService = inventoryService;
    }

    @Override
    @Transactional
    public StockTransferResponse create(CreateStockTransferRequest request) {
        if (stockTransferRepository.existsByOrganizationIdAndTransferNumber(request.organizationId(), request.transferNumber())) {
            throw new DuplicateResourceException(
                    "A stock transfer with number '" + request.transferNumber() + "' already exists for this organization.");
        }
        requireDifferentStores(request.fromStoreId(), request.toStoreId());
        Organization organization = organizationRepository.findById(request.organizationId())
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Organization", request.organizationId()));
        Store fromStore = requireStoreInOrganization(request.fromStoreId(), request.organizationId());
        Store toStore = requireStoreInOrganization(request.toStoreId(), request.organizationId());

        StockTransfer transfer = stockTransferMapper.toEntity(request);
        transfer.setOrganization(organization);
        transfer.setFromStore(fromStore);
        transfer.setToStore(toStore);
        transfer.setStatus(StockTransferStatus.DRAFT);
        StockTransfer saved = stockTransferRepository.save(transfer);
        return toResponse(saved, List.of());
    }

    @Override
    @Transactional
    public StockTransferResponse update(Long id, UpdateStockTransferRequest request) {
        StockTransfer transfer = getEntity(id);
        assertMutable(transfer);
        requireDifferentStores(request.fromStoreId(), request.toStoreId());
        if (!transfer.getTransferNumber().equals(request.transferNumber())
                && stockTransferRepository.existsByOrganizationIdAndTransferNumber(
                        transfer.getOrganization().getId(), request.transferNumber())) {
            throw new DuplicateResourceException(
                    "A stock transfer with number '" + request.transferNumber() + "' already exists for this organization.");
        }
        Store fromStore = requireStoreInOrganization(request.fromStoreId(), transfer.getOrganization().getId());
        Store toStore = requireStoreInOrganization(request.toStoreId(), transfer.getOrganization().getId());

        stockTransferMapper.updateEntityFromRequest(request, transfer);
        transfer.setFromStore(fromStore);
        transfer.setToStore(toStore);
        StockTransfer saved = stockTransferRepository.save(transfer);
        return toResponse(saved, currentLines(id));
    }

    @Override
    @Transactional(readOnly = true)
    public StockTransferResponse getById(Long id) {
        StockTransfer transfer = getEntity(id);
        return toResponse(transfer, currentLines(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StockTransferResponse> list(Long organizationId, StockTransferStatus status,
                                             Set<Long> storeIdFilter, Pageable pageable) {
        return stockTransferRepository
                .findAll(StockTransferSpecifications.filter(organizationId, status, storeIdFilter), pageable)
                .map(transfer -> toResponse(transfer, currentLines(transfer.getId())));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        StockTransfer transfer = getEntity(id);
        assertMutable(transfer);
        stockTransferLineRepository.deleteAllByStockTransferId(id);
        stockTransferRepository.delete(transfer);
    }

    @Override
    @Transactional
    public StockTransferResponse addLine(Long id, CreateStockTransferLineRequest request) {
        StockTransfer transfer = getEntity(id);
        assertMutable(transfer);
        Product product = requireProductInOrganization(request.productId(), transfer.getOrganization().getId());

        StockTransferLine line = new StockTransferLine();
        line.setStockTransfer(transfer);
        line.setProduct(product);
        line.setQuantity(request.quantity());
        stockTransferLineRepository.save(line);

        return toResponse(transfer, currentLines(id));
    }

    @Override
    @Transactional
    public StockTransferResponse updateLine(Long id, Long lineId, UpdateStockTransferLineRequest request) {
        StockTransfer transfer = getEntity(id);
        assertMutable(transfer);
        StockTransferLine line = stockTransferLineRepository.findByIdAndStockTransferId(lineId, id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("StockTransferLine", lineId));
        line.setQuantity(request.quantity());
        stockTransferLineRepository.save(line);

        return toResponse(transfer, currentLines(id));
    }

    @Override
    @Transactional
    public StockTransferResponse removeLine(Long id, Long lineId) {
        StockTransfer transfer = getEntity(id);
        assertMutable(transfer);
        StockTransferLine line = stockTransferLineRepository.findByIdAndStockTransferId(lineId, id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("StockTransferLine", lineId));
        stockTransferLineRepository.delete(line);

        return toResponse(transfer, currentLines(id));
    }

    @Override
    @Transactional
    public StockTransferResponse complete(Long id) {
        StockTransfer transfer = getEntity(id);
        assertMutable(transfer);
        List<StockTransferLine> lines = stockTransferLineRepository.findAllByStockTransferId(id);
        if (lines.isEmpty()) {
            throw new BusinessException("Cannot complete a stock transfer with no lines.");
        }

        for (StockTransferLine line : lines) {
            inventoryService.applyStockChange(
                    line.getProduct().getId(),
                    transfer.getFromStore().getId(),
                    line.getQuantity().negate(),
                    InventoryTransactionType.TRANSFER_OUT,
                    REFERENCE_TYPE,
                    transfer.getId(),
                    null);
            inventoryService.applyStockChange(
                    line.getProduct().getId(),
                    transfer.getToStore().getId(),
                    line.getQuantity(),
                    InventoryTransactionType.TRANSFER_IN,
                    REFERENCE_TYPE,
                    transfer.getId(),
                    null);
        }

        transfer.setStatus(StockTransferStatus.COMPLETED);
        StockTransfer saved = stockTransferRepository.save(transfer);
        return toResponse(saved, lines);
    }

    private void assertMutable(StockTransfer transfer) {
        if (transfer.getStatus() != StockTransferStatus.DRAFT) {
            throw new BusinessException("Stock transfer " + transfer.getId() + " is " + transfer.getStatus()
                    + " and can no longer be edited - post a stock adjustment to correct it instead.");
        }
    }

    private void requireDifferentStores(Long fromStoreId, Long toStoreId) {
        if (fromStoreId.equals(toStoreId)) {
            throw new BusinessException("A stock transfer's source and destination store must be different.");
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
        return product;
    }

    private List<StockTransferLine> currentLines(Long transferId) {
        return stockTransferLineRepository.findAllByStockTransferId(transferId);
    }

    private List<StockTransferLineResponse> mapLines(List<StockTransferLine> lines) {
        return lines.stream().map(stockTransferLineMapper::toResponse).toList();
    }

    private StockTransferResponse toResponse(StockTransfer transfer, List<StockTransferLine> lines) {
        return new StockTransferResponse(
                transfer.getId(),
                transfer.getOrganization().getId(),
                transfer.getTransferNumber(),
                transfer.getFromStore().getId(),
                transfer.getFromStore().getCode(),
                transfer.getFromStore().getName(),
                transfer.getToStore().getId(),
                transfer.getToStore().getCode(),
                transfer.getToStore().getName(),
                transfer.getTransferDate(),
                transfer.getStatus(),
                mapLines(lines),
                transfer.getCreatedDate(),
                transfer.getUpdatedDate());
    }

    private StockTransfer getEntity(Long id) {
        return stockTransferRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("StockTransfer", id));
    }
}
