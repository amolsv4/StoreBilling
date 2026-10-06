package com.retailpos.payment.controller;

import com.retailpos.auth.AccessValidator;
import com.retailpos.auth.userdetails.UserPrincipal;
import com.retailpos.common.dto.ApiResponse;
import com.retailpos.payment.dto.CreatePaymentRequest;
import com.retailpos.payment.dto.PaymentResponse;
import com.retailpos.payment.service.PaymentService;
import com.retailpos.sale.service.SaleAccessContext;
import com.retailpos.sale.service.SaleService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Access is re-checked against the loaded sale's store/terminal, same rule as {@code SaleController}. */
@RestController
@RequestMapping("/api/v1/sales/{saleId}/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final SaleService saleService;
    private final AccessValidator accessValidator;

    public PaymentController(PaymentService paymentService, SaleService saleService, AccessValidator accessValidator) {
        this.paymentService = paymentService;
        this.saleService = saleService;
        this.accessValidator = accessValidator;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PAYMENT_CREATE')")
    public ApiResponse<PaymentResponse> create(@PathVariable Long saleId,
                                                @Valid @RequestBody CreatePaymentRequest request,
                                                @AuthenticationPrincipal UserPrincipal principal) {
        assertAccess(saleId, principal);
        return ApiResponse.success("Payment recorded.", paymentService.create(saleId, request));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PAYMENT_VIEW')")
    public ApiResponse<Page<PaymentResponse>> list(@PathVariable Long saleId,
                                                    Pageable pageable,
                                                    @AuthenticationPrincipal UserPrincipal principal) {
        assertAccess(saleId, principal);
        return ApiResponse.success(paymentService.list(saleId, pageable));
    }

    private void assertAccess(Long saleId, UserPrincipal principal) {
        SaleAccessContext access = saleService.getAccessContext(saleId);
        accessValidator.assertStoreAccess(principal, access.storeId());
        accessValidator.assertTerminalAccess(principal, access.terminalId());
    }
}
