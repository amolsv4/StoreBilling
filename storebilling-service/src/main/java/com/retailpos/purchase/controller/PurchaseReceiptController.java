package com.retailpos.purchase.controller;

import com.retailpos.auth.AccessValidator;
import com.retailpos.auth.userdetails.UserPrincipal;
import com.retailpos.common.dto.ApiResponse;
import com.retailpos.inventory.util.StoreScopeResolver;
import com.retailpos.purchase.dto.CreatePurchaseReceiptLineRequest;
import com.retailpos.purchase.dto.CreatePurchaseReceiptRequest;
import com.retailpos.purchase.dto.PurchaseReceiptResponse;
import com.retailpos.purchase.dto.UpdatePurchaseReceiptLineRequest;
import com.retailpos.purchase.dto.UpdatePurchaseReceiptRequest;
import com.retailpos.purchase.entity.PurchaseReceiptStatus;
import com.retailpos.purchase.service.PurchaseReceiptService;
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
 * Header + incremental per-line endpoints (mirrors Sales' scan-by-scan model, not a bulk
 * header+lines payload). Every mutation is re-checked against the caller's store access using the
 * *loaded* receipt's {@code storeId} (not a URL/body param), so revoked store access takes effect
 * immediately even on line-level sub-resource calls.
 */
@RestController
@RequestMapping("/api/v1/purchase-receipts")
public class PurchaseReceiptController {

    private final PurchaseReceiptService purchaseReceiptService;
    private final AccessValidator accessValidator;

    public PurchaseReceiptController(PurchaseReceiptService purchaseReceiptService, AccessValidator accessValidator) {
        this.purchaseReceiptService = purchaseReceiptService;
        this.accessValidator = accessValidator;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PURCHASE_VIEW')")
    public ApiResponse<Page<PurchaseReceiptResponse>> list(@RequestParam(required = false) Long organizationId,
                                                               @RequestParam(required = false) Long storeId,
                                                               @RequestParam(required = false) PurchaseReceiptStatus status,
                                                               Pageable pageable,
                                                               @AuthenticationPrincipal UserPrincipal principal) {
        Set<Long> storeIdFilter = StoreScopeResolver.resolve(principal, storeId, accessValidator);
        return ApiResponse.success(purchaseReceiptService.list(organizationId, status, storeIdFilter, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PURCHASE_VIEW')")
    public ApiResponse<PurchaseReceiptResponse> getById(@PathVariable Long id,
                                                           @AuthenticationPrincipal UserPrincipal principal) {
        PurchaseReceiptResponse response = purchaseReceiptService.getById(id);
        accessValidator.assertStoreAccess(principal, response.storeId());
        return ApiResponse.success(response);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PURCHASE_CREATE')")
    public ApiResponse<PurchaseReceiptResponse> create(@Valid @RequestBody CreatePurchaseReceiptRequest request,
                                                          @AuthenticationPrincipal UserPrincipal principal) {
        accessValidator.assertStoreAccess(principal, request.storeId());
        return ApiResponse.success("Purchase receipt created.", purchaseReceiptService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PURCHASE_UPDATE')")
    public ApiResponse<PurchaseReceiptResponse> update(@PathVariable Long id,
                                                          @Valid @RequestBody UpdatePurchaseReceiptRequest request,
                                                          @AuthenticationPrincipal UserPrincipal principal) {
        assertAccess(id, principal);
        return ApiResponse.success("Purchase receipt updated.", purchaseReceiptService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PURCHASE_UPDATE')")
    public ApiResponse<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        assertAccess(id, principal);
        purchaseReceiptService.delete(id);
        return ApiResponse.success("Purchase receipt deleted.", null);
    }

    @PostMapping("/{id}/lines")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PURCHASE_UPDATE')")
    public ApiResponse<PurchaseReceiptResponse> addLine(@PathVariable Long id,
                                                           @Valid @RequestBody CreatePurchaseReceiptLineRequest request,
                                                           @AuthenticationPrincipal UserPrincipal principal) {
        assertAccess(id, principal);
        return ApiResponse.success("Purchase receipt line added.", purchaseReceiptService.addLine(id, request));
    }

    @PutMapping("/{id}/lines/{lineId}")
    @PreAuthorize("hasAuthority('PURCHASE_UPDATE')")
    public ApiResponse<PurchaseReceiptResponse> updateLine(@PathVariable Long id, @PathVariable Long lineId,
                                                              @Valid @RequestBody UpdatePurchaseReceiptLineRequest request,
                                                              @AuthenticationPrincipal UserPrincipal principal) {
        assertAccess(id, principal);
        return ApiResponse.success("Purchase receipt line updated.", purchaseReceiptService.updateLine(id, lineId, request));
    }

    @DeleteMapping("/{id}/lines/{lineId}")
    @PreAuthorize("hasAuthority('PURCHASE_UPDATE')")
    public ApiResponse<PurchaseReceiptResponse> removeLine(@PathVariable Long id, @PathVariable Long lineId,
                                                              @AuthenticationPrincipal UserPrincipal principal) {
        assertAccess(id, principal);
        return ApiResponse.success("Purchase receipt line removed.", purchaseReceiptService.removeLine(id, lineId));
    }

    @PostMapping("/{id}/receive")
    @PreAuthorize("hasAuthority('PURCHASE_RECEIVE')")
    public ApiResponse<PurchaseReceiptResponse> receive(@PathVariable Long id,
                                                           @AuthenticationPrincipal UserPrincipal principal) {
        assertAccess(id, principal);
        return ApiResponse.success("Purchase receipt received into inventory.", purchaseReceiptService.receive(id));
    }

    private void assertAccess(Long id, UserPrincipal principal) {
        PurchaseReceiptResponse existing = purchaseReceiptService.getById(id);
        accessValidator.assertStoreAccess(principal, existing.storeId());
    }
}
