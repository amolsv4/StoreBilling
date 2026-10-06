package com.retailpos.inventory.repository;

import com.retailpos.inventory.entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Long>, JpaSpecificationExecutor<Inventory> {

    /** Plain, non-locking read - used by {@code getCurrentStock} and list/report endpoints. */
    Optional<Inventory> findByProductIdAndStoreId(Long productId, Long storeId);

    /**
     * Row-level {@code SELECT ... FOR UPDATE} lookup used by
     * {@link com.retailpos.inventory.service.InventoryServiceImpl#applyStockChange} so two
     * concurrent mutations to the same {@code (productId, storeId)} balance serialize correctly.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Inventory i where i.productId = :productId and i.storeId = :storeId")
    Optional<Inventory> findByProductIdAndStoreIdForUpdate(@Param("productId") Long productId,
                                                            @Param("storeId") Long storeId);
}
