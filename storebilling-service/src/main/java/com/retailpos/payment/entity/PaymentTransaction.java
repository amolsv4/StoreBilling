package com.retailpos.payment.entity;

import com.retailpos.common.entity.BaseAuditableEntity;
import com.retailpos.sale.entity.Sale;
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

/**
 * A single tender recorded against a {@link Sale} (a sale has many rows for split tender). Full
 * {@code BaseAuditableEntity} (not append-only) - kept mutable so a future Phase 6 refund/
 * reversal can transition {@code status} in place.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payment_transaction")
public class PaymentTransaction extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Column(name = "amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.COMPLETED;
}
