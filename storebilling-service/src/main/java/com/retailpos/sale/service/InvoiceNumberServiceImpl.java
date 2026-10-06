package com.retailpos.sale.service;

import com.retailpos.sale.entity.InvoiceSequence;
import com.retailpos.sale.repository.InvoiceSequenceRepository;
import com.retailpos.store.entity.Store;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Concurrency-safety: {@link #generateNextInvoiceNumber} runs under Spring's default REQUIRED
 * propagation, so calling it from {@code SaleServiceImpl.complete()} (itself
 * {@code @Transactional}) joins that same transaction rather than opening a new one - the
 * {@code PESSIMISTIC_WRITE} row lock taken by {@code InvoiceSequenceRepository.findByIdForUpdate}
 * is therefore held for the whole sale-completion transaction, so two concurrent terminals under
 * the same store can never be assigned the same sequence number.
 */
@Service
public class InvoiceNumberServiceImpl implements InvoiceNumberService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final InvoiceSequenceRepository invoiceSequenceRepository;

    public InvoiceNumberServiceImpl(InvoiceSequenceRepository invoiceSequenceRepository) {
        this.invoiceSequenceRepository = invoiceSequenceRepository;
    }

    @Override
    @Transactional
    public String generateNextInvoiceNumber(Store store) {
        Long storeId = store.getId();
        invoiceSequenceRepository.upsertNoOp(storeId);
        InvoiceSequence sequence = invoiceSequenceRepository.findByIdForUpdate(storeId)
                .orElseThrow(() -> new IllegalStateException(
                        "Invoice sequence row missing for store id " + storeId + " immediately after upsert."));

        long assignedValue = sequence.getNextValue();
        sequence.setNextValue(assignedValue + 1);
        invoiceSequenceRepository.save(sequence);

        String datePart = LocalDate.now().format(DATE_FORMAT);
        return "%s-%s-%06d".formatted(store.getCode(), datePart, assignedValue);
    }
}
