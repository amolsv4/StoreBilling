package com.retailpos.payment.service;

import com.retailpos.payment.dto.CreatePaymentRequest;
import com.retailpos.payment.dto.PaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface PaymentService {

    /** Records one tender against the sale. Rejects (400) if the sale is already COMPLETED. */
    PaymentResponse create(Long saleId, CreatePaymentRequest request);

    Page<PaymentResponse> list(Long saleId, Pageable pageable);

    /** Sum of this sale's COMPLETED payments - zero if none exist. Used by SaleService.complete(). */
    BigDecimal sumCompletedAmount(Long saleId);
}
