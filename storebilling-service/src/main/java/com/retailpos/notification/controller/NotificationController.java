package com.retailpos.notification.controller;

import com.retailpos.auth.AccessValidator;
import com.retailpos.auth.userdetails.UserPrincipal;
import com.retailpos.common.dto.ApiResponse;
import com.retailpos.notification.dto.NotificationLogResponse;
import com.retailpos.notification.dto.SendNotificationRequest;
import com.retailpos.notification.service.SaleReceiptNotificationService;
import com.retailpos.sale.service.SaleAccessContext;
import com.retailpos.sale.service.SaleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Permission: reuses {@code SALES_VIEW} - any role that can see the sale can trigger sending its
 * receipt (per the plan's Section 0 item 2).
 */
@RestController
@RequestMapping("/api/v1/sales/{saleId}/notifications")
public class NotificationController {

    private final SaleReceiptNotificationService saleReceiptNotificationService;
    private final SaleService saleService;
    private final AccessValidator accessValidator;

    public NotificationController(SaleReceiptNotificationService saleReceiptNotificationService,
                                   SaleService saleService,
                                   AccessValidator accessValidator) {
        this.saleReceiptNotificationService = saleReceiptNotificationService;
        this.saleService = saleService;
        this.accessValidator = accessValidator;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('SALES_VIEW')")
    public ApiResponse<NotificationLogResponse> send(@PathVariable Long saleId,
                                                       @Valid @RequestBody SendNotificationRequest request,
                                                       @AuthenticationPrincipal UserPrincipal principal) {
        SaleAccessContext access = saleService.getAccessContext(saleId);
        accessValidator.assertStoreAccess(principal, access.storeId());
        accessValidator.assertTerminalAccess(principal, access.terminalId());
        return ApiResponse.success("Notification sent.",
                saleReceiptNotificationService.notifyReceipt(saleId, request.channel()));
    }
}
