package com.retailpos.sale.entity;

import com.retailpos.common.entity.BaseAuditableEntity;
import com.retailpos.customer.entity.Customer;
import com.retailpos.organization.entity.Organization;
import com.retailpos.store.entity.Store;
import com.retailpos.terminal.entity.Terminal;
import com.retailpos.user.entity.User;
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
import java.time.LocalDateTime;

/**
 * POS sale (transaction) header. {@code invoice_number} stays {@code null} until
 * {@link SaleStatus#COMPLETED} - {@code UNIQUE(store_id, invoice_number)} tolerates unlimited
 * NULLs in MySQL, so concurrent in-progress sales for the same store never collide on the key.
 *
 * <p>{@code soldByUser} is a business fact (who rang up the sale) - distinct from the technical
 * audit {@code createdBy} string inherited from {@link BaseAuditableEntity}, which records
 * whichever principal made the underlying HTTP call.
 *
 * <p>Sales/Payments are transactional records with their own documented lifecycle (see
 * {@link com.retailpos.sale.service.SaleService}), not master data - they deliberately do NOT
 * follow the generic soft-delete ({@code active} flag) convention used elsewhere in this app.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "sale")
public class Sale extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal_id", nullable = false)
    private Terminal terminal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sold_by_user_id", nullable = false)
    private User soldByUser;

    @Column(name = "sale_date", nullable = false)
    private LocalDateTime saleDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SaleStatus status = SaleStatus.DRAFT;

    @Column(name = "invoice_number", length = 50)
    private String invoiceNumber;

    /** Snapshot of {@code DEFAULT_CURRENCY} resolved via {@code ConfigurationService} at creation time. */
    @Column(name = "currency", nullable = false, length = 10)
    private String currency;

    @Column(name = "subtotal", nullable = false, precision = 14, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    /** Bill-level discount only - line-level discounts are already baked into each line's {@code lineTotal}. */
    @Column(name = "discount_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;
}
