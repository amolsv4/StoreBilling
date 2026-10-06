package com.retailpos.stocktransfer.controller;

import com.retailpos.auth.AccessValidator;
import com.retailpos.auth.userdetails.UserPrincipal;
import com.retailpos.common.dto.ApiResponse;
import com.retailpos.inventory.util.StoreScopeResolver;
import com.retailpos.stocktransfer.dto.CreateStockTransferLineRequest;
import com.retailpos.stocktransfer.dto.CreateStockTransferRequest;
import com.retailpos.stocktransfer.dto.StockTransferResponse;
import com.retailpos.stocktransfer.dto.UpdateStockTransferLineRequest;
import com.retailpos.stocktransfer.dto.UpdateStockTransferRequest;
import com.retailpos.stocktransfer.entity.StockTransferStatus;
import com.retailpos.stocktransfer.service.StockTransferService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * Header + incremental per-line endpoints, same shape as {@code PurchaseReceiptController}.
 * Because a transfer touches two stores, every mutation checks {@link AccessValidator} against
 * BOTH the from-store and the to-store - a caller with access to only one leg must not be able to
 * create, edit, or complete the transfer.
 */
@RestController
@RequestMapping("/api/v1/stock-transfers")
public class StockTransferController {

    private final StockTransferService stockTransferService;
    private final AccessValidator accessValidator;

    public StockTransferController(StockTransferService stockTransferService, AccessValidator accessValidator) {
        this.stockTransferService = stockTransferService;
        this.accessValidator = accessValidator;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('TRANSFER_VIEW')")
    public ApiResponse<Page<StockTransferResponse>> list(@RequestParam(required = false) Long organizationId,
                                                             @RequestParam(required = false) Long storeId,
                                                             @RequestParam(required = false) StockTransferStatus status,
                                                             Pageable pageable,
                                                             @AuthenticationPrincipal UserPrincipal principal) {
        Set<Long> storeIdFilter = StoreScopeResolver.resolve(principal, storeId, accessValidator);
        return ApiResponse.success(stockTransferService.list(organizationId, status, storeIdFilter, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TRANSFER_VIEW')")
    public ApiResponse<StockTransferResponse> getById(@PathVariable Long id,
                                                          @AuthenticationPrincipal UserPrincipal principal) {
        StockTransferResponse response = stockTransferService.getById(id);
        assertBothStoreAccess(principal, response);
        return ApiResponse.success(response);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('TRANSFER_CREATE')")
    public ApiResponse<StockTransferResponse> create(@Valid @RequestBody CreateStockTransferRequest request,
                                                         @AuthenticationPrincipal UserPrincipal principal) {
        accessValidator.assertStoreAccess(principal, request.fromStoreId());
        accessValidator.assertStoreAccess(principal, request.toStoreId());
        return ApiResponse.success("Stock transfer created.", stockTransferService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TRANSFER_UPDATE')")
    public ApiResponse<StockTransferResponse> update(@PathVariable Long id,
                                                         @Valid @RequestBody UpdateStockTransferRequest request,
                                                         @AuthenticationPrincipal UserPrincipal principal) {
        assertAccess(id, principal);
        accessValidator.assertStoreAccess(principal, request.fromStoreId());
        accessValidator.assertStoreAccess(principal, request.toStoreId());
        return ApiResponse.success("Stock transfer updated.", stockTransferService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('TRANSFER_UPDATE')")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        assertAccess(id, principal);
        stockTransferService.delete(id);
        return ApiResponse.success("Stock transfer deleted.", null);
    }

    @PostMapping("/{id}/lines")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('TRANSFER_UPDATE')")
    public ApiResponse<StockTransferResponse> addLine(@PathVariable Long id,
                                                          @Valid @RequestBody CreateStockTransferLineRequest request,
                                                          @AuthenticationPrincipal UserPrincipal principal) {
        assertAccess(id, principal);
        return ApiResponse.success("Stock transfer line added.", stockTransferService.addLine(id, request));
    }

    @PutMapping("/{id}/lines/{lineId}")
    @PreAuthorize("hasAuthority('TRANSFER_UPDATE')")
    public ApiResponse<StockTransferResponse> updateLine(@PathVariable Long id, @PathVariable Long lineId,
                                                             @Valid @RequestBody UpdateStockTransferLineRequest request,
                                                             @AuthenticationPrincipal UserPrincipal principal) {
        assertAccess(id, principal);
        return ApiResponse.success("Stock transfer line updated.", stockTransferService.updateLine(id, lineId, request));
    }

    @DeleteMapping("/{id}/lines/{lineId}")
    @PreAuthorize("hasAuthority('TRANSFER_UPDATE')")
    public ApiResponse<StockTransferResponse> removeLine(@PathVariable Long id, @PathVariable Long lineId,
                                                             @AuthenticationPrincipal UserPrincipal principal) {
        assertAccess(id, principal);
        return ApiResponse.success("Stock transfer line removed.", stockTransferService.removeLine(id, lineId));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('TRANSFER_COMPLETE')")
    public ApiResponse<StockTransferResponse> complete(@PathVariable Long id,
                                                           @AuthenticationPrincipal UserPrincipal principal) {
        assertAccess(id, principal);
        return ApiResponse.success("Stock transfer completed.", stockTransferService.complete(id));
    }

    private void assertAccess(Long id, UserPrincipal principal) {
        StockTransferResponse existing = stockTransferService.getById(id);
        assertBothStoreAccess(principal, existing);
    }

    private void assertBothStoreAccess(UserPrincipal principal, StockTransferResponse response) {
        accessValidator.assertStoreAccess(principal, response.fromStoreId());
        accessValidator.assertStoreAccess(principal, response.toStoreId());
    }
}
