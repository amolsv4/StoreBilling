package com.retailpos.stocktransfer.entity;

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
 * Stock transfer header, moving stock between two stores of the same organization. Mutable while
 * {@code DRAFT}; immutable once {@code COMPLETED} - see {@link StockTransferStatus}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "stock_transfer")
public class StockTransfer extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "transfer_number", nullable = false, length = 30)
    private String transferNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_store_id", nullable = false)
    private Store fromStore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_store_id", nullable = false)
    private Store toStore;

    @Column(name = "transfer_date", nullable = false)
    private LocalDate transferDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StockTransferStatus status = StockTransferStatus.DRAFT;
}
