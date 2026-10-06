package com.retailpos.stocktake.entity;

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

import java.time.LocalDate;

/**
 * Stock take (physical inventory count) header for a single store. Mutable while {@code DRAFT};
 * immutable once {@code COMPLETED} - see {@link StockTakeStatus}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "stock_take")
public class StockTake extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "stock_take_number", nullable = false, length = 30)
    private String stockTakeNumber;

    @Column(name = "stock_take_date", nullable = false)
    private LocalDate stockTakeDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StockTakeStatus status = StockTakeStatus.DRAFT;
}
