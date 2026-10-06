package com.retailpos.payment.service;

import com.retailpos.common.exception.BusinessException;
import com.retailpos.common.exception.ResourceNotFoundException;
import com.retailpos.payment.dto.CreatePaymentRequest;
import com.retailpos.payment.dto.PaymentResponse;
import com.retailpos.payment.entity.PaymentStatus;
import com.retailpos.payment.entity.PaymentTransaction;
import com.retailpos.payment.mapper.PaymentMapper;
import com.retailpos.payment.repository.PaymentTransactionRepository;
import com.retailpos.sale.entity.Sale;
import com.retailpos.sale.entity.SaleStatus;
import com.retailpos.sale.repository.SaleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Depends only on {@link SaleRepository} (not {@code SaleService}) to load the parent sale -
 * {@code SaleServiceImpl} depends on {@link PaymentService} (for {@link #sumCompletedAmount}) at
 * completion time, so keeping this dependency one-directional avoids a circular bean graph.
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentTransactionRepository paymentTransactionRepository;
    private final SaleRepository saleRepository;
    private final PaymentMapper paymentMapper;

    public PaymentServiceImpl(PaymentTransactionRepository paymentTransactionRepository,
                               SaleRepository saleRepository,
                               PaymentMapper paymentMapper) {
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.saleRepository = saleRepository;
        this.paymentMapper = paymentMapper;
    }

    @Override
    @Transactional
    public PaymentResponse create(Long saleId, CreatePaymentRequest request) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Sale", saleId));
        if (sale.getStatus() == SaleStatus.COMPLETED) {
            throw new BusinessException("Cannot record a payment against a completed sale.");
        }

        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setSale(sale);
        transaction.setPaymentMethod(request.paymentMethod());
        transaction.setAmount(request.amount());
        transaction.setReferenceNumber(request.referenceNumber());
        transaction.setStatus(PaymentStatus.COMPLETED);
        PaymentTransaction saved = paymentTransactionRepository.save(transaction);
        return paymentMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> list(Long saleId, Pageable pageable) {
        return paymentTransactionRepository.findAllBySaleId(saleId, pageable).map(paymentMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal sumCompletedAmount(Long saleId) {
        return paymentTransactionRepository.sumAmountBySaleIdAndStatus(saleId, PaymentStatus.COMPLETED);
    }
}
