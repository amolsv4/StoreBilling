package com.retailpos.stocktake.service;

import com.retailpos.stocktake.dto.CreateStockTakeLineRequest;
import com.retailpos.stocktake.dto.CreateStockTakeRequest;
import com.retailpos.stocktake.dto.StockTakeResponse;
import com.retailpos.stocktake.dto.UpdateStockTakeLineRequest;
import com.retailpos.stocktake.dto.UpdateStockTakeRequest;
import com.retailpos.stocktake.entity.StockTakeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface StockTakeService {

    StockTakeResponse create(CreateStockTakeRequest request);

    StockTakeResponse update(Long id, UpdateStockTakeRequest request);

    StockTakeResponse getById(Long id);

    /** @param storeIdFilter {@code null} = unrestricted; see {@code StoreScopeResolver}. */
    Page<StockTakeResponse> list(Long organizationId, StockTakeStatus status, Set<Long> storeIdFilter, Pageable pageable);

    void delete(Long id);

    /** Snapshots {@code systemQuantity} server-side from the live balance at add-time. */
    StockTakeResponse addLine(Long id, CreateStockTakeLineRequest request);

    StockTakeResponse updateLine(Long id, Long lineId, UpdateStockTakeLineRequest request);

    StockTakeResponse removeLine(Long id, Long lineId);

    /**
     * Recomputes each line's variance against the LIVE balance at completion time (not the stale
     * draft-time snapshot) and posts a {@code STOCK_TAKE} ledger entry only where that recomputed
     * variance is non-zero.
     */
    StockTakeResponse complete(Long id);
}
