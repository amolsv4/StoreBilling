package com.retailpos.inventory.service;

import com.retailpos.common.exception.BusinessException;
import com.retailpos.common.exception.ResourceNotFoundException;
import com.retailpos.inventory.dto.CreateStockAdjustmentRequest;
import com.retailpos.inventory.dto.StockAdjustmentResponse;
import com.retailpos.inventory.entity.InventoryTransaction;
import com.retailpos.inventory.entity.StockAdjustment;
import com.retailpos.inventory.entity.StockAdjustmentType;
import com.retailpos.inventory.repository.InventoryTransactionRepository;
import com.retailpos.inventory.repository.StockAdjustmentRepository;
import com.retailpos.inventory.repository.StockAdjustmentSpecifications;
import com.retailpos.product.entity.Product;
import com.retailpos.product.repository.ProductRepository;
import com.retailpos.store.entity.Store;
import com.retailpos.store.repository.StoreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StockAdjustmentServiceImpl implements StockAdjustmentService {

    private static final String REFERENCE_TYPE = "STOCK_ADJUSTMENT";

    private final StockAdjustmentRepository stockAdjustmentRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final InventoryService inventoryService;

    public StockAdjustmentServiceImpl(StockAdjustmentRepository stockAdjustmentRepository,
                                       InventoryTransactionRepository inventoryTransactionRepository,
                                       ProductRepository productRepository,
                                       StoreRepository storeRepository,
                                       InventoryService inventoryService) {
        this.stockAdjustmentRepository = stockAdjustmentRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.inventoryService = inventoryService;
    }

    @Override
    @Transactional
    public StockAdjustmentResponse create(CreateStockAdjustmentRequest request) {
        if ((request.adjustmentType() == StockAdjustmentType.DAMAGE
                || request.adjustmentType() == StockAdjustmentType.EXPIRY)
                && request.quantityChange().signum() > 0) {
            throw new BusinessException(
                    "A " + request.adjustmentType() + " adjustment cannot have a positive quantity change "
                            + "(there is no such thing as \"negative damage\").");
        }

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Product", request.productId()));
        Store store = storeRepository.findById(request.storeId())
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Store", request.storeId()));

        StockAdjustment adjustment = new StockAdjustment();
        adjustment.setStoreId(request.storeId());
        adjustment.setProductId(request.productId());
        adjustment.setAdjustmentType(request.adjustmentType());
        adjustment.setQuantityChange(request.quantityChange());
        adjustment.setReason(request.reason());
        StockAdjustment saved = stockAdjustmentRepository.save(adjustment);

        StockChangeResult result = inventoryService.applyStockChange(
                request.productId(),
                request.storeId(),
                request.quantityChange(),
                InventoryTransactionType.valueOf(request.adjustmentType().name()),
                REFERENCE_TYPE,
                saved.getId(),
                request.reason());

        return toResponse(saved, product, store, result.balanceAfter());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StockAdjustmentResponse> list(Long productId, StockAdjustmentType adjustmentType,
                                               Set<Long> storeIdFilter, Pageable pageable) {
        Page<StockAdjustment> page = stockAdjustmentRepository.findAll(
                StockAdjustmentSpecifications.filter(productId, adjustmentType, storeIdFilter), pageable);

        List<Long> adjustmentIds = page.getContent().stream().map(StockAdjustment::getId).toList();
        List<Long> productIds = page.getContent().stream().map(StockAdjustment::getProductId).filter(Objects::nonNull).distinct().toList();
        List<Long> storeIds = page.getContent().stream().map(StockAdjustment::getStoreId).filter(Objects::nonNull).distinct().toList();
        Map<Long, Product> products = productIds.isEmpty() ? Map.of()
                : productRepository.findAllById(productIds).stream().collect(Collectors.toMap(Product::getId, p -> p));
        Map<Long, Store> stores = storeIds.isEmpty() ? Map.of()
                : storeRepository.findAllById(storeIds).stream().collect(Collectors.toMap(Store::getId, s -> s));
        // Each stock_adjustment posts exactly one matching inventory_transaction row (see create()
        // above) - look those up by reference to show the TRUE historical balance-after for each
        // adjustment, rather than today's current balance (which would be misleading for older rows).
        Map<Long, BigDecimal> balanceAfterByAdjustmentId = adjustmentIds.isEmpty() ? Map.of()
                : inventoryTransactionRepository.findAllByReferenceTypeAndReferenceIdIn(REFERENCE_TYPE, adjustmentIds).stream()
                        .collect(Collectors.toMap(InventoryTransaction::getReferenceId, InventoryTransaction::getBalanceAfter));

        return page.map(adj -> toResponse(adj, products.get(adj.getProductId()), stores.get(adj.getStoreId()),
                balanceAfterByAdjustmentId.get(adj.getId())));
    }

    private StockAdjustmentResponse toResponse(StockAdjustment adjustment, Product product, Store store,
                                                BigDecimal balanceAfter) {
        return new StockAdjustmentResponse(
                adjustment.getId(),
                adjustment.getStoreId(),
                store != null ? store.getCode() : null,
                store != null ? store.getName() : null,
                adjustment.getProductId(),
                product != null ? product.getSku() : null,
                product != null ? product.getName() : null,
                adjustment.getAdjustmentType(),
                adjustment.getQuantityChange(),
                balanceAfter,
                adjustment.getReason(),
                adjustment.getCreatedBy(),
                adjustment.getCreatedDate());
    }
}
