package com.retailpos.purchase.entity;

import com.retailpos.common.entity.BaseAuditableEntity;
import com.retailpos.organization.entity.Organization;
import com.retailpos.store.entity.Store;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Purchase receipt header. No structured Supplier master exists yet (or is justified by this
 * phase's scope - no AP/payables/returns workflow) - {@code supplierName} is deliberately free
 * text. Mutable (header and lines) while {@code DRAFT}; immutable once {@code RECEIVED} - see
 * {@link PurchaseReceiptStatus}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "purchase_receipt")
public class PurchaseReceipt extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "supplier_name", nullable = false, length = 150)
    private String supplierName;

    @Column(name = "receipt_number", nullable = false, length = 30)
    private String receiptNumber;

    @Column(name = "receipt_date", nullable = false)
    private LocalDate receiptDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PurchaseReceiptStatus status = PurchaseReceiptStatus.DRAFT;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;
}
