package com.retailpos.stocktransfer.repository;

import com.retailpos.stocktransfer.entity.StockTransferLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockTransferLineRepository extends JpaRepository<StockTransferLine, Long> {

    List<StockTransferLine> findAllByStockTransferId(Long stockTransferId);

    Optional<StockTransferLine> findByIdAndStockTransferId(Long id, Long stockTransferId);

    void deleteAllByStockTransferId(Long stockTransferId);
}
