package com.retailpos.purchase.entity;

import com.retailpos.common.entity.BaseAuditableEntity;
import com.retailpos.product.entity.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * A single product/quantity/cost line of a {@link PurchaseReceipt}. Managed via its own
 * incremental per-line endpoints (add/update/remove one at a time), not as part of a bulk
 * header+lines payload - mirrors Sales' scan-by-scan cart model. Hard-deletable while the parent
 * receipt is {@code DRAFT} only.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "purchase_receipt_line")
public class PurchaseReceiptLine extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_receipt_id", nullable = false)
    private PurchaseReceipt purchaseReceipt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "line_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal lineTotal;
}
