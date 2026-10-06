package com.retailpos.purchase.service;

import com.retailpos.purchase.dto.CreatePurchaseReceiptLineRequest;
import com.retailpos.purchase.dto.CreatePurchaseReceiptRequest;
import com.retailpos.purchase.dto.PurchaseReceiptResponse;
import com.retailpos.purchase.dto.UpdatePurchaseReceiptLineRequest;
import com.retailpos.purchase.dto.UpdatePurchaseReceiptRequest;
import com.retailpos.purchase.entity.PurchaseReceiptStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface PurchaseReceiptService {

    PurchaseReceiptResponse create(CreatePurchaseReceiptRequest request);

    PurchaseReceiptResponse update(Long id, UpdatePurchaseReceiptRequest request);

    PurchaseReceiptResponse getById(Long id);

    /** @param storeIdFilter {@code null} = unrestricted; see {@code StoreScopeResolver}. */
    Page<PurchaseReceiptResponse> list(Long organizationId, PurchaseReceiptStatus status,
                                        Set<Long> storeIdFilter, Pageable pageable);

    void delete(Long id);

    PurchaseReceiptResponse addLine(Long id, CreatePurchaseReceiptLineRequest request);

    PurchaseReceiptResponse updateLine(Long id, Long lineId, UpdatePurchaseReceiptLineRequest request);

    PurchaseReceiptResponse removeLine(Long id, Long lineId);

    /** Posts every line to the inventory ledger (type=PURCHASE) and locks the header from further edits. */
    PurchaseReceiptResponse receive(Long id);
}
