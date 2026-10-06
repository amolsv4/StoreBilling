package com.retailpos.inventory.service;

import com.retailpos.inventory.entity.Inventory;
import com.retailpos.inventory.entity.InventoryTransaction;
import com.retailpos.inventory.repository.InventoryRepository;
import com.retailpos.inventory.repository.InventoryTransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * The single choke point for every stock mutation in the system - see {@link InventoryService}'s
 * javadoc for the full contract. Runs under Spring's default REQUIRED propagation so it joins
 * whatever transaction the caller is already in.
 */
@Service
public class InventoryServiceImpl implements InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryServiceImpl.class);

    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;

    public InventoryServiceImpl(InventoryRepository inventoryRepository,
                                 InventoryTransactionRepository inventoryTransactionRepository) {
        this.inventoryRepository = inventoryRepository;
        this.inventoryTransactionRepository = inventoryTransactionRepository;
    }

    @Override
    @Transactional
    public StockChangeResult applyStockChange(Long productId,
                                               Long storeId,
                                               BigDecimal quantityChange,
                                               InventoryTransactionType type,
                                               String referenceType,
                                               Long referenceId,
                                               String notes) {
        Assert.notNull(productId, "productId is required");
        Assert.notNull(storeId, "storeId is required");
        Assert.notNull(quantityChange, "quantityChange is required");
        Assert.notNull(type, "type is required");

        Inventory balance = lockOrCreateBalance(productId, storeId);
        BigDecimal newBalance = balance.getQuantityOnHand().add(quantityChange);
        balance.setQuantityOnHand(newBalance);
        inventoryRepository.save(balance);

        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setProductId(productId);
        transaction.setStoreId(storeId);
        transaction.setTransactionType(type);
        transaction.setQuantityChange(quantityChange);
        transaction.setBalanceAfter(newBalance);
        transaction.setReferenceType(referenceType);
        transaction.setReferenceId(referenceId);
        transaction.setNotes(notes);
        InventoryTransaction saved = inventoryTransactionRepository.save(transaction);

        if (newBalance.signum() < 0) {
            log.warn("Inventory balance for product {} at store {} went negative ({}) after a {} "
                            + "transaction (reference {}:{}) - allowed by design, flagged for reconciliation.",
                    productId, storeId, newBalance, type, referenceType, referenceId);
        }

        return new StockChangeResult(saved.getId(), newBalance);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCurrentStock(Long productId, Long storeId) {
        Assert.notNull(productId, "productId is required");
        Assert.notNull(storeId, "storeId is required");
        return inventoryRepository.findByProductIdAndStoreId(productId, storeId)
                .map(Inventory::getQuantityOnHand)
                .orElse(zero());
    }

    /**
     * Locks (or creates, at zero) the {@code (productId, storeId)} balance row. The first-insert
     * race between two concurrent callers that both miss the initial lookup is guarded by the
     * table's own {@code UNIQUE(product_id, store_id)} constraint: the loser's insert fails with a
     * {@link DataIntegrityViolationException}, which is caught here and retried exactly once via
     * the locked lookup (now guaranteed to find the winner's row). This relies on MySQL/InnoDB not
     * poisoning the rest of the enclosing transaction after a single duplicate-key error on an
     * INSERT (unlike, say, PostgreSQL, which would require the retry to happen in a separate
     * transaction/savepoint) - safe here because this service's only database is MySQL.
     */
    private Inventory lockOrCreateBalance(Long productId, Long storeId) {
        Optional<Inventory> existing = inventoryRepository.findByProductIdAndStoreIdForUpdate(productId, storeId);
        if (existing.isPresent()) {
            return existing.get();
        }
        try {
            Inventory zeroBalance = new Inventory();
            zeroBalance.setProductId(productId);
            zeroBalance.setStoreId(storeId);
            zeroBalance.setQuantityOnHand(zero());
            return inventoryRepository.saveAndFlush(zeroBalance);
        } catch (DataIntegrityViolationException raceLost) {
            return inventoryRepository.findByProductIdAndStoreIdForUpdate(productId, storeId)
                    .orElseThrow(() -> raceLost);
        }
    }

    private static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(3);
    }
}
