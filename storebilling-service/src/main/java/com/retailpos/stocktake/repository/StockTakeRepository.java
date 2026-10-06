package com.retailpos.stocktake.repository;

import com.retailpos.stocktake.entity.StockTake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StockTakeRepository extends JpaRepository<StockTake, Long>, JpaSpecificationExecutor<StockTake> {

    boolean existsByOrganizationIdAndStockTakeNumber(Long organizationId, String stockTakeNumber);
}
