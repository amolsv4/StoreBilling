package com.retailpos.inventory.service;

import com.retailpos.inventory.dto.CreateStockAdjustmentRequest;
import com.retailpos.inventory.dto.StockAdjustmentResponse;
import com.retailpos.inventory.entity.StockAdjustmentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

/** Create-only - {@code stock_adjustment} is append-only, with no update/delete endpoint. */
public interface StockAdjustmentService {

    StockAdjustmentResponse create(CreateStockAdjustmentRequest request);

    /** @param storeIdFilter {@code null} = unrestricted; see {@code StoreScopeResolver}. */
    Page<StockAdjustmentResponse> list(Long productId, StockAdjustmentType adjustmentType,
                                        Set<Long> storeIdFilter, Pageable pageable);
}
