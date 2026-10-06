package com.retailpos.sale.entity;

import com.retailpos.common.entity.BaseAuditableEntity;
import com.retailpos.product.entity.Product;
import com.retailpos.product.entity.UnitOfMeasure;
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
 * One line of a {@link Sale}. {@code productName}/{@code sku}/{@code uom} and
 * {@code taxGroupId}/{@code taxPercentage} are deliberate point-in-time snapshots taken when the
 * line is added, so a completed invoice never silently changes if the product is later renamed
 * or a tax rate is edited. {@code taxGroupId} is a plain FK column (no JPA association) for the
 * same reason - it is never re-read live once snapshotted.
 *
 * <p>Quantity is {@code DECIMAL(12,3)}, not an integer - product UOMs include KG/GRAM/LITER.
 *
 * <p>Deliberate, documented exception to the soft-delete convention: a line may be hard-deleted
 * by {@code SaleServiceImpl.deleteLine} while its parent sale is not yet
 * {@link SaleStatus#COMPLETED} - an in-progress cart line was never a finalized record.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "sale_line")
public class SaleLine extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "product_name", nullable = false, length = 150)
    private String productName;

    @Column(name = "sku", nullable = false, length = 50)
    private String sku;

    @Enumerated(EnumType.STRING)
    @Column(name = "uom", nullable = false, length = 10)
    private UnitOfMeasure uom;

    @Column(name = "quantity", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "price_overridden", nullable = false)
    private boolean priceOverridden = false;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    /** Snapshot of the product's tax group id at add-time (plain FK column, see class javadoc). */
    @Column(name = "tax_group_id")
    private Long taxGroupId;

    /** Snapshot of {@code TaxGroupService.getEffectiveTotalPercentage} at add-time. */
    @Column(name = "tax_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxPercentage = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;
}
