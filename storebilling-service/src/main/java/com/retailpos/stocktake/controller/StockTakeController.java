package com.retailpos.stocktake.controller;

import com.retailpos.auth.AccessValidator;
import com.retailpos.auth.userdetails.UserPrincipal;
import com.retailpos.common.dto.ApiResponse;
import com.retailpos.inventory.util.StoreScopeResolver;
import com.retailpos.stocktake.dto.CreateStockTakeLineRequest;
import com.retailpos.stocktake.dto.CreateStockTakeRequest;
import com.retailpos.stocktake.dto.StockTakeResponse;
import com.retailpos.stocktake.dto.UpdateStockTakeLineRequest;
import com.retailpos.stocktake.dto.UpdateStockTakeRequest;
import com.retailpos.stocktake.entity.StockTakeStatus;
import com.retailpos.stocktake.service.StockTakeService;
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

/** Header + incremental per-line endpoints, same shape as {@code PurchaseReceiptController}. */
@RestController
@RequestMapping("/api/v1/stock-takes")
public class StockTakeController {

    private final StockTakeService stockTakeService;
    private final AccessValidator accessValidator;

    public StockTakeController(StockTakeService stockTakeService, AccessValidator accessValidator) {
        this.stockTakeService = stockTakeService;
        this.accessValidator = accessValidator;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('STOCK_TAKE_VIEW')")
    public ApiResponse<Page<StockTakeResponse>> list(@RequestParam(required = false) Long organizationId,
                                                         @RequestParam(required = false) Long storeId,
                                                         @RequestParam(required = false) StockTakeStatus status,
                                                         Pageable pageable,
                                                         @AuthenticationPrincipal UserPrincipal principal) {
        Set<Long> storeIdFilter = StoreScopeResolver.resolve(principal, storeId, accessValidator);
        return ApiResponse.success(stockTakeService.list(organizationId, status, storeIdFilter, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('STOCK_TAKE_VIEW')")
    public ApiResponse<StockTakeResponse> getById(@PathVariable Long id,
                                                     @AuthenticationPrincipal UserPrincipal principal) {
        StockTakeResponse response = stockTakeService.getById(id);
        accessValidator.assertStoreAccess(principal, response.storeId());
        return ApiResponse.success(response);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('STOCK_TAKE_CREATE')")
    public ApiResponse<StockTakeResponse> create(@Valid @RequestBody CreateStockTakeRequest request,
                                                    @AuthenticationPrincipal UserPrincipal principal) {
        accessValidator.assertStoreAccess(principal, request.storeId());
        return ApiResponse.success("Stock take created.", stockTakeService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('STOCK_TAKE_UPDATE')")
    public ApiResponse<StockTakeResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody UpdateStockTakeRequest request,
                                                    @AuthenticationPrincipal UserPrincipal principal) {
        assertAccess(id, principal);
        return ApiResponse.success("Stock take updated.", stockTakeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('STOCK_TAKE_UPDATE')")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        assertAccess(id, principal);
        stockTakeService.delete(id);
        return ApiResponse.success("Stock take deleted.", null);
    }

    @PostMapping("/{id}/lines")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('STOCK_TAKE_UPDATE')")
    public ApiResponse<StockTakeResponse> addLine(@PathVariable Long id,
                                                     @Valid @RequestBody CreateStockTakeLineRequest request,
                                                     @AuthenticationPrincipal UserPrincipal principal) {
        assertAccess(id, principal);
        return ApiResponse.success("Stock take line added.", stockTakeService.addLine(id, request));
    }

    @PutMapping("/{id}/lines/{lineId}")
    @PreAuthorize("hasAuthority('STOCK_TAKE_UPDATE')")
    public ApiResponse<StockTakeResponse> updateLine(@PathVariable Long id, @PathVariable Long lineId,
                                                        @Valid @RequestBody UpdateStockTakeLineRequest request,
                                                        @AuthenticationPrincipal UserPrincipal principal) {
        assertAccess(id, principal);
        return ApiResponse.success("Stock take line updated.", stockTakeService.updateLine(id, lineId, request));
    }

    @DeleteMapping("/{id}/lines/{lineId}")
    @PreAuthorize("hasAuthority('STOCK_TAKE_UPDATE')")
    public ApiResponse<StockTakeResponse> removeLine(@PathVariable Long id, @PathVariable Long lineId,
                                                        @AuthenticationPrincipal UserPrincipal principal) {
        assertAccess(id, principal);
        return ApiResponse.success("Stock take line removed.", stockTakeService.removeLine(id, lineId));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('STOCK_TAKE_COMPLETE')")
    public ApiResponse<StockTakeResponse> complete(@PathVariable Long id,
                                                      @AuthenticationPrincipal UserPrincipal principal) {
        assertAccess(id, principal);
        return ApiResponse.success("Stock take completed.", stockTakeService.complete(id));
    }

    private void assertAccess(Long id, UserPrincipal principal) {
        StockTakeResponse existing = stockTakeService.getById(id);
        accessValidator.assertStoreAccess(principal, existing.storeId());
    }
}
