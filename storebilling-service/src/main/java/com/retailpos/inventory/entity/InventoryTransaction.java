package com.retailpos.inventory.entity;

import com.retailpos.inventory.service.InventoryTransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Append-only inventory ledger entry - one row per
 * {@link com.retailpos.inventory.service.InventoryService#applyStockChange} call. Deliberately
 * does NOT extend {@link com.retailpos.common.entity.BaseAuditableEntity} - same shape as
 * {@link com.retailpos.audit.entity.AuditLog}: no {@code updated_by}/{@code updated_date}/
 * {@code version}, since rows are never modified after insert.
 *
 * <p>{@code referenceType}/{@code referenceId} are a deliberately open-ended, polymorphic pointer
 * (no CHECK constraint, no FK) - the table they name depends on {@code transactionType}, e.g.
 * {@code "PURCHASE_RECEIPT"}, {@code "SALE"}, {@code "STOCK_TRANSFER"}, {@code "STOCK_ADJUSTMENT"},
 * {@code "STOCK_TAKE"}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "inventory_transaction")
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private InventoryTransactionType transactionType;

    @Column(name = "quantity_change", nullable = false, precision = 14, scale = 3)
    private BigDecimal quantityChange;

    @Column(name = "balance_after", nullable = false, precision = 14, scale = 3)
    private BigDecimal balanceAfter;

    @Column(name = "reference_type", length = 30)
    private String referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "notes", length = 500)
    private String notes;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false, length = 50)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;
}
