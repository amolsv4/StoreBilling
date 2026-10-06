package com.retailpos.stocktake.entity;

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
 * A single counted product line of a {@link StockTake}. {@code systemQuantity} is snapshotted
 * server-side from {@code InventoryService#getCurrentStock} at the moment the line is added -
 * never client-supplied. {@code variance} holds the draft-time display value
 * ({@code countedQuantity - systemQuantity}) until {@code complete()} recomputes and overwrites it
 * against the *live* balance (see {@code StockTakeServiceImpl#complete}).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "stock_take_line")
public class StockTakeLine extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_take_id", nullable = false)
    private StockTake stockTake;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "system_quantity", nullable = false, precision = 12, scale = 3)
    private BigDecimal systemQuantity;

    @Column(name = "counted_quantity", nullable = false, precision = 12, scale = 3)
    private BigDecimal countedQuantity;

    @Column(name = "variance", nullable = false, precision = 12, scale = 3)
    private BigDecimal variance;
}
