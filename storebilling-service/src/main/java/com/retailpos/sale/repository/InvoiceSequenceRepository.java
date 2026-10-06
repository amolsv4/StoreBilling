package com.retailpos.sale.repository;

import com.retailpos.sale.entity.InvoiceSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface InvoiceSequenceRepository extends JpaRepository<InvoiceSequence, Long> {

    /**
     * Row-locks the store's sequence for the duration of the caller's transaction, so two
     * concurrent terminals completing a sale under the same store never read/increment the same
     * {@code next_value}. Must be called inside the same {@code @Transactional} method that
     * completes the sale (see {@code InvoiceNumberServiceImpl}).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM InvoiceSequence s WHERE s.storeId = :storeId")
    Optional<InvoiceSequence> findByIdForUpdate(@Param("storeId") Long storeId);

    /**
     * Atomic upsert-no-op: inserts a starting row ({@code next_value = 1}) if the store has never
     * had an invoice before, otherwise leaves the existing row untouched. Self-heals a missing
     * row on first use - no seed migration is needed for this table.
     */
    @Modifying
    @Query(value = "INSERT INTO invoice_sequence (store_id, next_value) VALUES (:storeId, 1) "
            + "ON DUPLICATE KEY UPDATE store_id = store_id", nativeQuery = true)
    void upsertNoOp(@Param("storeId") Long storeId);
}
