package com.retailpos.inventory.controller;

import com.retailpos.auth.AccessValidator;
import com.retailpos.auth.userdetails.UserPrincipal;
import com.retailpos.common.dto.ApiResponse;
import com.retailpos.inventory.dto.CreateStockAdjustmentRequest;
import com.retailpos.inventory.dto.StockAdjustmentResponse;
import com.retailpos.inventory.entity.StockAdjustmentType;
import com.retailpos.inventory.service.StockAdjustmentService;
import com.retailpos.inventory.util.StoreScopeResolver;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/** Create-only - {@code stock_adjustment} is append-only, so there is no update/delete endpoint. */
@RestController
@RequestMapping("/api/v1/stock-adjustments")
public class StockAdjustmentController {

    private final StockAdjustmentService stockAdjustmentService;
    private final AccessValidator accessValidator;

    public StockAdjustmentController(StockAdjustmentService stockAdjustmentService, AccessValidator accessValidator) {
        this.stockAdjustmentService = stockAdjustmentService;
        this.accessValidator = accessValidator;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ApiResponse<Page<StockAdjustmentResponse>> list(@RequestParam(required = false) Long storeId,
                                                              @RequestParam(required = false) Long productId,
                                                              @RequestParam(required = false) StockAdjustmentType adjustmentType,
                                                              Pageable pageable,
                                                              @AuthenticationPrincipal UserPrincipal principal) {
        Set<Long> storeIdFilter = StoreScopeResolver.resolve(principal, storeId, accessValidator);
        return ApiResponse.success(stockAdjustmentService.list(productId, adjustmentType, storeIdFilter, pageable));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('INVENTORY_ADJUST')")
    public ApiResponse<StockAdjustmentResponse> create(@Valid @RequestBody CreateStockAdjustmentRequest request,
                                                          @AuthenticationPrincipal UserPrincipal principal) {
        accessValidator.assertStoreAccess(principal, request.storeId());
        return ApiResponse.success("Stock adjustment recorded.", stockAdjustmentService.create(request));
    }
}
