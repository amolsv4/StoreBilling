package com.retailpos.inventory.service;

import com.retailpos.inventory.dto.InventoryResponse;
import com.retailpos.inventory.dto.InventoryTransactionResponse;
import com.retailpos.inventory.entity.Inventory;
import com.retailpos.inventory.entity.InventoryTransaction;
import com.retailpos.inventory.repository.InventoryRepository;
import com.retailpos.inventory.repository.InventorySpecifications;
import com.retailpos.inventory.repository.InventoryTransactionRepository;
import com.retailpos.inventory.repository.InventoryTransactionSpecifications;
import com.retailpos.product.entity.Product;
import com.retailpos.product.repository.ProductRepository;
import com.retailpos.store.entity.Store;
import com.retailpos.store.repository.StoreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * {@link Inventory}/{@link InventoryTransaction} store only plain {@code productId}/
 * {@code storeId} columns (no JPA association - see their javadoc), so display fields
 * (product SKU/name, store code/name) are joined here in the service layer via batched
 * {@code findAllById} lookups rather than a MapStruct {@code @Mapping(source = "product.sku")}
 * path, which is why this module has no MapStruct mapper for these two response types.
 */
@Service
public class InventoryQueryServiceImpl implements InventoryQueryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;

    public InventoryQueryServiceImpl(InventoryRepository inventoryRepository,
                                      InventoryTransactionRepository inventoryTransactionRepository,
                                      ProductRepository productRepository,
                                      StoreRepository storeRepository) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryResponse> list(Long productId, Set<Long> storeIdFilter, Pageable pageable) {
        Page<Inventory> page = inventoryRepository.findAll(
                InventorySpecifications.filter(productId, storeIdFilter), pageable);
        Map<Long, Product> products = fetchByIds(page.getContent(), Inventory::getProductId, productRepository::findAllById, Product::getId);
        Map<Long, Store> stores = fetchByIds(page.getContent(), Inventory::getStoreId, storeRepository::findAllById, Store::getId);
        return page.map(inv -> toResponse(inv, products.get(inv.getProductId()), stores.get(inv.getStoreId())));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InventoryTransactionResponse> listTransactions(Long productId, InventoryTransactionType type,
                                                                 Set<Long> storeIdFilter, Pageable pageable) {
        Page<InventoryTransaction> page = inventoryTransactionRepository.findAll(
                InventoryTransactionSpecifications.filter(productId, type, storeIdFilter), pageable);
        Map<Long, Product> products = fetchByIds(page.getContent(), InventoryTransaction::getProductId, productRepository::findAllById, Product::getId);
        Map<Long, Store> stores = fetchByIds(page.getContent(), InventoryTransaction::getStoreId, storeRepository::findAllById, Store::getId);
        return page.map(txn -> toResponse(txn, products.get(txn.getProductId()), stores.get(txn.getStoreId())));
    }

    private <T, E> Map<Long, E> fetchByIds(List<T> rows, Function<T, Long> idExtractor,
                                            Function<Iterable<Long>, List<E>> finder, Function<E, Long> entityId) {
        List<Long> ids = rows.stream().map(idExtractor).filter(Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        return finder.apply(ids).stream().collect(Collectors.toMap(entityId, e -> e));
    }

    private InventoryResponse toResponse(Inventory inventory, Product product, Store store) {
        return new InventoryResponse(
                inventory.getId(),
                inventory.getProductId(),
                product != null ? product.getSku() : null,
                product != null ? product.getName() : null,
                inventory.getStoreId(),
                store != null ? store.getCode() : null,
                store != null ? store.getName() : null,
                inventory.getQuantityOnHand(),
                inventory.getUpdatedBy(),
                inventory.getUpdatedDate());
    }

    private InventoryTransactionResponse toResponse(InventoryTransaction transaction, Product product, Store store) {
        return new InventoryTransactionResponse(
                transaction.getId(),
                transaction.getProductId(),
                product != null ? product.getSku() : null,
                product != null ? product.getName() : null,
                transaction.getStoreId(),
                store != null ? store.getCode() : null,
                store != null ? store.getName() : null,
                transaction.getTransactionType(),
                transaction.getQuantityChange(),
                transaction.getBalanceAfter(),
                transaction.getReferenceType(),
                transaction.getReferenceId(),
                transaction.getNotes(),
                transaction.getCreatedBy(),
                transaction.getCreatedDate());
    }
}
