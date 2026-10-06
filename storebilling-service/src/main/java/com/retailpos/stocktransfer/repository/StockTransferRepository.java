package com.retailpos.stocktransfer.repository;

import com.retailpos.stocktransfer.entity.StockTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface StockTransferRepository
        extends JpaRepository<StockTransfer, Long>, JpaSpecificationExecutor<StockTransfer> {

    boolean existsByOrganizationIdAndTransferNumber(Long organizationId, String transferNumber);
}
