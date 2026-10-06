package com.retailpos.sale.service;

import com.retailpos.sale.dto.AddSaleLineRequest;
import com.retailpos.sale.dto.CompleteSaleResponse;
import com.retailpos.sale.dto.CreateSaleRequest;
import com.retailpos.sale.dto.SaleResponse;
import com.retailpos.sale.dto.UpdateSaleDiscountRequest;
import com.retailpos.sale.dto.UpdateSaleLineRequest;
import com.retailpos.sale.entity.SaleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SaleService {

    SaleResponse create(CreateSaleRequest request, Long soldByUserId);

    Page<SaleResponse> list(SaleStatus status, Long storeId, Long terminalId, Pageable pageable);

    SaleResponse getById(Long id);

    /** (storeId, terminalId) of the loaded sale, for controller-side IDOR checks. */
    SaleAccessContext getAccessContext(Long saleId);

    SaleResponse addLine(Long saleId, AddSaleLineRequest request, boolean callerHasPriceOverrideAuthority);

    SaleResponse updateLine(Long saleId, Long lineId, UpdateSaleLineRequest request, boolean callerHasPriceOverrideAuthority);

    /** Hard-deletes the line (deliberate exception to the soft-delete convention) - only while the sale is not COMPLETED. */
    SaleResponse deleteLine(Long saleId, Long lineId);

    SaleResponse updateDiscount(Long saleId, UpdateSaleDiscountRequest request);

    SaleResponse hold(Long saleId);

    SaleResponse resume(Long saleId);

    /**
     * Idempotent: if the sale is already COMPLETED, returns the existing state unchanged
     * (recomputes nothing, reassigns no invoice number, re-posts no inventory). Otherwise
     * verifies payments cover the total, assigns an invoice number, posts one inventory
     * transaction per line, and marks the sale COMPLETED.
     */
    CompleteSaleResponse complete(Long saleId);
}
