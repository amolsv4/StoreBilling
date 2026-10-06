package com.retailpos.inventory.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Current stock balance for a single {@code (productId, storeId)} pair. Deliberately does NOT
 * extend {@link com.retailpos.common.entity.BaseAuditableEntity} - a balance row has no meaningful
 * "created" event distinct from its first ledger entry (see
 * {@link com.retailpos.inventory.service.InventoryServiceImpl#applyStockChange}, which upserts this
 * row from zero on first use), so there is no {@code created_by}/{@code created_date} column at
 * all, only {@code updated_by}/{@code updated_date}/{@code version}.
 *
 * <p>{@code productId}/{@code storeId} are plain columns, not {@code @ManyToOne} associations -
 * this row sits on the hot path of every stock mutation and read (locked on every
 * {@code applyStockChange} call), and never needs to navigate the full Product/Store object graph.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "quantity_on_hand", nullable = false, precision = 14, scale = 3)
    private BigDecimal quantityOnHand = BigDecimal.ZERO;

    @LastModifiedBy
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @LastModifiedDate
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
