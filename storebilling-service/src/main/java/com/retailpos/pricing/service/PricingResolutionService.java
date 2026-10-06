package com.retailpos.pricing.service;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Resolves the effective selling price of a product at a given store, used by Sales when a
 * sale line's {@code unitPrice} is not explicitly supplied by the caller.
 */
public interface PricingResolutionService {

    /**
     * Resolution algorithm: loads the store's {@code defaultPriceListId} (throws
     * {@link com.retailpos.common.exception.BusinessException} if the store has none configured -
     * a configuration problem, not a "price not found" outcome), finds the single
     * {@code (priceListId, productId)} {@code ProductPrice} row, and checks it is active and
     * {@code asOfDate} falls within its effective-date range.
     *
     * @return the resolved price, or {@link Optional#empty()} if no active/in-range
     * {@code ProductPrice} row exists for this product in the store's default price list.
     */
    Optional<ResolvedPrice> resolveEffectivePrice(Long productId, Long storeId, LocalDate asOfDate);
}
