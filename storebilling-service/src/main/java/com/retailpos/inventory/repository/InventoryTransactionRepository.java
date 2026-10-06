package com.retailpos.inventory.repository;

import com.retailpos.inventory.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface InventoryTransactionRepository
        extends JpaRepository<InventoryTransaction, Long>, JpaSpecificationExecutor<InventoryTransaction> {

    /**
     * Used to look up the exact ledger row(s) posted for a given polymorphic reference (e.g. a
     * batch of {@code stock_adjustment} ids) so callers can show the true historical
     * {@code balanceAfter} for that reference instead of today's current balance.
     */
    List<InventoryTransaction> findAllByReferenceTypeAndReferenceIdIn(String referenceType, List<Long> referenceIds);
}
