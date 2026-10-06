package com.retailpos.sale.controller;

import com.retailpos.auth.AccessValidator;
import com.retailpos.auth.userdetails.UserPrincipal;
import com.retailpos.common.dto.ApiResponse;
import com.retailpos.sale.dto.AddSaleLineRequest;
import com.retailpos.sale.dto.CompleteSaleResponse;
import com.retailpos.sale.dto.CreateSaleRequest;
import com.retailpos.sale.dto.SaleResponse;
import com.retailpos.sale.dto.UpdateSaleDiscountRequest;
import com.retailpos.sale.dto.UpdateSaleLineRequest;
import com.retailpos.sale.entity.SaleStatus;
import com.retailpos.sale.service.SaleAccessContext;
import com.retailpos.sale.service.SaleService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.GrantedAuthority;
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

/**
 * Access is re-checked on every mutating endpoint via {@link AccessValidator}, keyed off the
 * *loaded* sale's {@code store_id}/{@code terminal_id} (not a URL param) so revoked store access
 * takes effect immediately even on sub-resource calls (plan reconciliation, Sales §2).
 */
@RestController
@RequestMapping("/api/v1/sales")
public class SaleController {

    private static final String PRICE_OVERRIDE_AUTHORITY = "SALES_PRICE_OVERRIDE";

    private final SaleService saleService;
    private final AccessValidator accessValidator;

    public SaleController(SaleService saleService, AccessValidator accessValidator) {
        this.saleService = saleService;
        this.accessValidator = accessValidator;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SALES_CREATE')")
    public ApiResponse<SaleResponse> create(@Valid @RequestBody CreateSaleRequest request,
                                             @AuthenticationPrincipal UserPrincipal principal) {
        accessValidator.assertStoreAccess(principal, request.storeId());
        accessValidator.assertTerminalAccess(principal, request.terminalId());
        return ApiResponse.success("Sale created.", saleService.create(request, principal.getId()));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SALES_VIEW')")
    public ApiResponse<Page<SaleResponse>> list(@RequestParam(required = false) SaleStatus status,
                                                 @RequestParam(required = false) Long storeId,
                                                 @RequestParam(required = false) Long terminalId,
                                                 Pageable pageable,
                                                 @AuthenticationPrincipal UserPrincipal principal) {
        accessValidator.assertStoreAccess(principal, storeId);
        accessValidator.assertTerminalAccess(principal, terminalId);
        return ApiResponse.success(saleService.list(status, storeId, terminalId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SALES_VIEW')")
    public ApiResponse<SaleResponse> getById(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        SaleResponse sale = saleService.getById(id);
        accessValidator.assertStoreAccess(principal, sale.storeId());
        accessValidator.assertTerminalAccess(principal, sale.terminalId());
        return ApiResponse.success(sale);
    }

    @PostMapping("/{id}/lines")
    @PreAuthorize("hasAuthority('SALES_CREATE')")
    public ApiResponse<SaleResponse> addLine(@PathVariable Long id,
                                              @Valid @RequestBody AddSaleLineRequest request,
                                              @AuthenticationPrincipal UserPrincipal principal) {
        assertAccess(id, principal);
        return ApiResponse.success("Line added.",
                saleService.addLine(id, request, hasAuthority(principal, PRICE_OVERRIDE_AUTHORITY)));
    }

    @PutMapping("/{id}/lines/{lineId}")
    @PreAuthorize("hasAuthority('SALES_UPDATE')")
    public ApiResponse<SaleResponse> updateLine(@PathVariable Long id,
                                                 @PathVariable Long lineId,
                                                 @Valid @RequestBody UpdateSaleLineRequest request,
                                                 @AuthenticationPrincipal UserPrincipal principal) {
        assertAccess(id, principal);
        return ApiResponse.success("Line updated.",
                saleService.updateLine(id, lineId, request, hasAuthority(principal, PRICE_OVERRIDE_AUTHORITY)));
    }

    /** Genuine hard delete, but only while the sale is not yet COMPLETED (see SaleServiceImpl). */
    @DeleteMapping("/{id}/lines/{lineId}")
    @PreAuthorize("hasAuthority('SALES_UPDATE')")
    public ApiResponse<SaleResponse> deleteLine(@PathVariable Long id,
                                                 @PathVariable Long lineId,
                                                 @AuthenticationPrincipal UserPrincipal principal) {
        assertAccess(id, principal);
        return ApiResponse.success("Line deleted.", saleService.deleteLine(id, lineId));
    }

    @PutMapping("/{id}/discount")
    @PreAuthorize("hasAuthority('SALES_UPDATE')")
    public ApiResponse<SaleResponse> updateDiscount(@PathVariable Long id,
                                                     @Valid @RequestBody UpdateSaleDiscountRequest request,
                                                     @AuthenticationPrincipal UserPrincipal principal) {
        assertAccess(id, principal);
        return ApiResponse.success("Discount updated.", saleService.updateDiscount(id, request));
    }

    @PostMapping("/{id}/hold")
    @PreAuthorize("hasAuthority('SALES_UPDATE')")
    public ApiResponse<SaleResponse> hold(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        assertAccess(id, principal);
        return ApiResponse.success("Sale held.", saleService.hold(id));
    }

    @PostMapping("/{id}/resume")
    @PreAuthorize("hasAuthority('SALES_UPDATE')")
    public ApiResponse<SaleResponse> resume(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        assertAccess(id, principal);
        return ApiResponse.success("Sale resumed.", saleService.resume(id));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('SALES_COMPLETE')")
    public ApiResponse<CompleteSaleResponse> complete(@PathVariable Long id,
                                                       @AuthenticationPrincipal UserPrincipal principal) {
        assertAccess(id, principal);
        return ApiResponse.success("Sale completed.", saleService.complete(id));
    }

    private void assertAccess(Long saleId, UserPrincipal principal) {
        SaleAccessContext access = saleService.getAccessContext(saleId);
        accessValidator.assertStoreAccess(principal, access.storeId());
        accessValidator.assertTerminalAccess(principal, access.terminalId());
    }

    static boolean hasAuthority(UserPrincipal principal, String authority) {
        for (GrantedAuthority granted : principal.getAuthorities()) {
            if (authority.equals(granted.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
