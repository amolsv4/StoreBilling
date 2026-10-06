package com.retailpos.inventory.controller;

import com.retailpos.auth.AccessValidator;
import com.retailpos.auth.userdetails.UserPrincipal;
import com.retailpos.common.dto.ApiResponse;
import com.retailpos.inventory.dto.InventoryResponse;
import com.retailpos.inventory.service.InventoryQueryService;
import com.retailpos.inventory.util.StoreScopeResolver;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/** Read-only current stock balances. Mutations only ever happen via {@code InventoryService}. */
@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryQueryService inventoryQueryService;
    private final AccessValidator accessValidator;

    public InventoryController(InventoryQueryService inventoryQueryService, AccessValidator accessValidator) {
        this.inventoryQueryService = inventoryQueryService;
        this.accessValidator = accessValidator;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ApiResponse<Page<InventoryResponse>> list(@RequestParam(required = false) Long storeId,
                                                        @RequestParam(required = false) Long productId,
                                                        Pageable pageable,
                                                        @AuthenticationPrincipal UserPrincipal principal) {
        Set<Long> storeIdFilter = StoreScopeResolver.resolve(principal, storeId, accessValidator);
        return ApiResponse.success(inventoryQueryService.list(productId, storeIdFilter, pageable));
    }
}
