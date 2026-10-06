package com.retailpos.pricing.service;

import java.math.BigDecimal;

/** Outcome of {@link PricingResolutionService#resolveEffectivePrice}. */
public record ResolvedPrice(Long priceListId, BigDecimal basePrice, BigDecimal sellingPrice) {
}
