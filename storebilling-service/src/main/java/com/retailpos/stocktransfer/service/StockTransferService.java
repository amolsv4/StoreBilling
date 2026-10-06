package com.retailpos.stocktransfer.service;

import com.retailpos.stocktransfer.dto.CreateStockTransferLineRequest;
import com.retailpos.stocktransfer.dto.CreateStockTransferRequest;
import com.retailpos.stocktransfer.dto.StockTransferResponse;
import com.retailpos.stocktransfer.dto.UpdateStockTransferLineRequest;
import com.retailpos.stocktransfer.dto.UpdateStockTransferRequest;
import com.retailpos.stocktransfer.entity.StockTransferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface StockTransferService {

    StockTransferResponse create(CreateStockTransferRequest request);

    StockTransferResponse update(Long id, UpdateStockTransferRequest request);

    StockTransferResponse getById(Long id);

    /** @param storeIdFilter {@code null} = unrestricted; see {@code StoreScopeResolver}. */
    Page<StockTransferResponse> list(Long organizationId, StockTransferStatus status,
                                      Set<Long> storeIdFilter, Pageable pageable);

    void delete(Long id);

    StockTransferResponse addLine(Long id, CreateStockTransferLineRequest request);

    StockTransferResponse updateLine(Long id, Long lineId, UpdateStockTransferLineRequest request);

    StockTransferResponse removeLine(Long id, Long lineId);

    /** Posts TRANSFER_OUT at the source store and TRANSFER_IN at the destination, atomically, per line. */
    StockTransferResponse complete(Long id);
}
