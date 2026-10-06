package com.retailpos.sale.service;

import com.retailpos.store.entity.Store;

public interface InvoiceNumberService {

    /**
     * Assigns and returns the next invoice number for {@code store}, formatted
     * {@code {storeCode}-{yyyyMMdd}-{sequence:06d}}. Must be called inside the same
     * {@code @Transactional} method that completes the sale, so the row lock it takes is held
     * for the duration of that transaction.
     */
    String generateNextInvoiceNumber(Store store);
}
