package com.retailpos.stocktake.service;

import com.retailpos.common.exception.BusinessException;
import com.retailpos.common.exception.DuplicateResourceException;
import com.retailpos.common.exception.ResourceNotFoundException;
import com.retailpos.inventory.service.InventoryService;
import com.retailpos.inventory.service.InventoryTransactionType;
import com.retailpos.organization.entity.Organization;
import com.retailpos.organization.repository.OrganizationRepository;
import com.retailpos.product.entity.Product;
import com.retailpos.product.repository.ProductRepository;
import com.retailpos.stocktake.dto.CreateStockTakeLineRequest;
import com.retailpos.stocktake.dto.CreateStockTakeRequest;
import com.retailpos.stocktake.dto.StockTakeLineResponse;
import com.retailpos.stocktake.dto.StockTakeResponse;
import com.retailpos.stocktake.dto.UpdateStockTakeLineRequest;
import com.retailpos.stocktake.dto.UpdateStockTakeRequest;
import com.retailpos.stocktake.entity.StockTake;
import com.retailpos.stocktake.entity.StockTakeLine;
import com.retailpos.stocktake.entity.StockTakeStatus;
import com.retailpos.stocktake.mapper.StockTakeLineMapper;
import com.retailpos.stocktake.mapper.StockTakeMapper;
import com.retailpos.stocktake.repository.StockTakeLineRepository;
import com.retailpos.stocktake.repository.StockTakeRepository;
import com.retailpos.stocktake.repository.StockTakeSpecifications;
import com.retailpos.store.entity.Store;
import com.retailpos.store.repository.StoreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
public class StockTakeServiceImpl implements StockTakeService {

    private static final String REFERENCE_TYPE = "STOCK_TAKE";

    private final StockTakeRepository stockTakeRepository;
    private final StockTakeLineRepository stockTakeLineRepository;
    private final OrganizationRepository organizationRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final StockTakeMapper stockTakeMapper;
    private final StockTakeLineMapper stockTakeLineMapper;
    private final InventoryService inventoryService;

    public StockTakeServiceImpl(StockTakeRepository stockTakeRepository,
                                 StockTakeLineRepository stockTakeLineRepository,
                                 OrganizationRepository organizationRepository,
                                 StoreRepository storeRepository,
                                 ProductRepository productRepository,
                                 StockTakeMapper stockTakeMapper,
                                 StockTakeLineMapper stockTakeLineMapper,
                                 InventoryService inventoryService) {
        this.stockTakeRepository = stockTakeRepository;
        this.stockTakeLineRepository = stockTakeLineRepository;
        this.organizationRepository = organizationRepository;
        this.storeRepository = storeRepository;
        this.productRepository = productRepository;
        this.stockTakeMapper = stockTakeMapper;
        this.stockTakeLineMapper = stockTakeLineMapper;
        this.inventoryService = inventoryService;
    }

    @Override
    @Transactional
    public StockTakeResponse create(CreateStockTakeRequest request) {
        if (stockTakeRepository.existsByOrganizationIdAndStockTakeNumber(request.organizationId(), request.stockTakeNumber())) {
            throw new DuplicateResourceException(
                    "A stock take with number '" + request.stockTakeNumber() + "' already exists for this organization.");
        }
        Organization organization = organizationRepository.findById(request.organizationId())
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Organization", request.organizationId()));
        Store store = requireStoreInOrganization(request.storeId(), request.organizationId());

        StockTake stockTake = stockTakeMapper.toEntity(request);
        stockTake.setOrganization(organization);
        stockTake.setStore(store);
        stockTake.setStatus(StockTakeStatus.DRAFT);
        StockTake saved = stockTakeRepository.save(stockTake);
        return toResponse(saved, List.of());
    }

    @Override
    @Transactional
    public StockTakeResponse update(Long id, UpdateStockTakeRequest request) {
        StockTake stockTake = getEntity(id);
        assertMutable(stockTake);
        if (!stockTake.getStockTakeNumber().equals(request.stockTakeNumber())
                && stockTakeRepository.existsByOrganizationIdAndStockTakeNumber(
                        stockTake.getOrganization().getId(), request.stockTakeNumber())) {
            throw new DuplicateResourceException(
                    "A stock take with number '" + request.stockTakeNumber() + "' already exists for this organization.");
        }
        stockTakeMapper.updateEntityFromRequest(request, stockTake);
        StockTake saved = stockTakeRepository.save(stockTake);
        return toResponse(saved, currentLines(id));
    }

    @Override
    @Transactional(readOnly = true)
    public StockTakeResponse getById(Long id) {
        StockTake stockTake = getEntity(id);
        return toResponse(stockTake, currentLines(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StockTakeResponse> list(Long organizationId, StockTakeStatus status, Set<Long> storeIdFilter, Pageable pageable) {
        return stockTakeRepository
                .findAll(StockTakeSpecifications.filter(organizationId, status, storeIdFilter), pageable)
                .map(stockTake -> toResponse(stockTake, currentLines(stockTake.getId())));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        StockTake stockTake = getEntity(id);
        assertMutable(stockTake);
        stockTakeLineRepository.deleteAllByStockTakeId(id);
        stockTakeRepository.delete(stockTake);
    }

    @Override
    @Transactional
    public StockTakeResponse addLine(Long id, CreateStockTakeLineRequest request) {
        StockTake stockTake = getEntity(id);
        assertMutable(stockTake);
        Product product = requireProductInOrganization(request.productId(), stockTake.getOrganization().getId());

        // system_quantity is snapshotted here, server-side, from the live balance - never client-supplied.
        BigDecimal systemQuantity = inventoryService.getCurrentStock(product.getId(), stockTake.getStore().getId());

        StockTakeLine line = new StockTakeLine();
        line.setStockTake(stockTake);
        line.setProduct(product);
        line.setSystemQuantity(systemQuantity);
        line.setCountedQuantity(request.countedQuantity());
        line.setVariance(request.countedQuantity().subtract(systemQuantity));
        stockTakeLineRepository.save(line);

        return toResponse(stockTake, currentLines(id));
    }

    @Override
    @Transactional
    public StockTakeResponse updateLine(Long id, Long lineId, UpdateStockTakeLineRequest request) {
        StockTake stockTake = getEntity(id);
        assertMutable(stockTake);
        StockTakeLine line = stockTakeLineRepository.findByIdAndStockTakeId(lineId, id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("StockTakeLine", lineId));

        line.setCountedQuantity(request.countedQuantity());
        line.setVariance(request.countedQuantity().subtract(line.getSystemQuantity()));
        stockTakeLineRepository.save(line);

        return toResponse(stockTake, currentLines(id));
    }

    @Override
    @Transactional
    public StockTakeResponse removeLine(Long id, Long lineId) {
        StockTake stockTake = getEntity(id);
        assertMutable(stockTake);
        StockTakeLine line = stockTakeLineRepository.findByIdAndStockTakeId(lineId, id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("StockTakeLine", lineId));
        stockTakeLineRepository.delete(line);

        return toResponse(stockTake, currentLines(id));
    }

    @Override
    @Transactional
    public StockTakeResponse complete(Long id) {
        StockTake stockTake = getEntity(id);
        assertMutable(stockTake);
        List<StockTakeLine> lines = stockTakeLineRepository.findAllByStockTakeId(id);
        if (lines.isEmpty()) {
            throw new BusinessException("Cannot complete a stock take with no lines.");
        }

        for (StockTakeLine line : lines) {
            // Handles drift between count-time and completion-time: recompute variance against the
            // LIVE balance right now, not the stale draft-time snapshot, so the post-completion
            // balance always equals the physical count regardless of sales/purchases/etc. that
            // happened during the count window.
            BigDecimal liveBalance = inventoryService.getCurrentStock(line.getProduct().getId(), stockTake.getStore().getId());
            BigDecimal actualVariance = line.getCountedQuantity().subtract(liveBalance);
            line.setVariance(actualVariance);
            stockTakeLineRepository.save(line);

            if (actualVariance.signum() != 0) {
                inventoryService.applyStockChange(
                        line.getProduct().getId(),
                        stockTake.getStore().getId(),
                        actualVariance,
                        InventoryTransactionType.STOCK_TAKE,
                        REFERENCE_TYPE,
                        stockTake.getId(),
                        null);
            }
        }

        stockTake.setStatus(StockTakeStatus.COMPLETED);
        StockTake saved = stockTakeRepository.save(stockTake);
        return toResponse(saved, lines);
    }

    private void assertMutable(StockTake stockTake) {
        if (stockTake.getStatus() != StockTakeStatus.DRAFT) {
            throw new BusinessException("Stock take " + stockTake.getId() + " is " + stockTake.getStatus()
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
        return product;
    }

    private List<StockTakeLine> currentLines(Long stockTakeId) {
        return stockTakeLineRepository.findAllByStockTakeId(stockTakeId);
    }

    private List<StockTakeLineResponse> mapLines(List<StockTakeLine> lines) {
        return lines.stream().map(stockTakeLineMapper::toResponse).toList();
    }

    private StockTakeResponse toResponse(StockTake stockTake, List<StockTakeLine> lines) {
        return new StockTakeResponse(
                stockTake.getId(),
                stockTake.getOrganization().getId(),
                stockTake.getStore().getId(),
                stockTake.getStore().getCode(),
                stockTake.getStore().getName(),
                stockTake.getStockTakeNumber(),
                stockTake.getStockTakeDate(),
                stockTake.getStatus(),
                mapLines(lines),
                stockTake.getCreatedDate(),
                stockTake.getUpdatedDate());
    }

    private StockTake getEntity(Long id) {
        return stockTakeRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("StockTake", id));
    }
}
