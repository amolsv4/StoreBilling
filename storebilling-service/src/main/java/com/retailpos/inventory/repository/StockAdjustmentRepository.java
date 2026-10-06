package com.retailpos.inventory.repository;

import com.retailpos.inventory.entity.StockAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StockAdjustmentRepository
        extends JpaRepository<StockAdjustment, Long>, JpaSpecificationExecutor<StockAdjustment> {
}
