package com.retailpos.stocktake.repository;

import com.retailpos.stocktake.entity.StockTakeLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StockTakeLineRepository extends JpaRepository<StockTakeLine, Long> {

    List<StockTakeLine> findAllByStockTakeId(Long stockTakeId);

    Optional<StockTakeLine> findByIdAndStockTakeId(Long id, Long stockTakeId);

    void deleteAllByStockTakeId(Long stockTakeId);
}
