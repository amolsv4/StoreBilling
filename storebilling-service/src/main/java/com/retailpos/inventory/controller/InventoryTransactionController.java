package com.retailpos.inventory.controller;

import com.retailpos.auth.AccessValidator;
import com.retailpos.auth.userdetails.UserPrincipal;
import com.retailpos.common.dto.ApiResponse;
import com.retailpos.inventory.dto.InventoryTransactionResponse;
import com.retailpos.inventory.service.InventoryQueryService;
import com.retailpos.inventory.service.InventoryTransactionType;
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

/** Read-only ledger history. Rows are append-only and written exclusively via {@code InventoryService}. */
@RestController
@RequestMapping("/api/v1/inventory-transactions")
public class InventoryTransactionController {

    private final InventoryQueryService inventoryQueryService;
    private final AccessValidator accessValidator;

    public InventoryTransactionController(InventoryQueryService inventoryQueryService, AccessValidator accessValidator) {
        this.inventoryQueryService = inventoryQueryService;
        this.accessValidator = accessValidator;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ApiResponse<Page<InventoryTransactionResponse>> list(@RequestParam(required = false) Long storeId,
                                                                  @RequestParam(required = false) Long productId,
                                                                  @RequestParam(required = false) InventoryTransactionType type,
                                                                  Pageable pageable,
                                                                  @AuthenticationPrincipal UserPrincipal principal) {
        Set<Long> storeIdFilter = StoreScopeResolver.resolve(principal, storeId, accessValidator);
        return ApiResponse.success(inventoryQueryService.listTransactions(productId, type, storeIdFilter, pageable));
    }
}
