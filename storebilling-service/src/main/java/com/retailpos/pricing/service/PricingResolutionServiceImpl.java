package com.retailpos.pricing.service;

import com.retailpos.common.exception.BusinessException;
import com.retailpos.common.exception.ResourceNotFoundException;
import com.retailpos.pricing.entity.ProductPrice;
import com.retailpos.pricing.repository.ProductPriceRepository;
import com.retailpos.store.entity.Store;
import com.retailpos.store.repository.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class PricingResolutionServiceImpl implements PricingResolutionService {

    private final StoreRepository storeRepository;
    private final ProductPriceRepository productPriceRepository;

    public PricingResolutionServiceImpl(StoreRepository storeRepository,
                                         ProductPriceRepository productPriceRepository) {
        this.storeRepository = storeRepository;
        this.productPriceRepository = productPriceRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ResolvedPrice> resolveEffectivePrice(Long productId, Long storeId, LocalDate asOfDate) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Store", storeId));
        Long priceListId = store.getDefaultPriceListId();
        if (priceListId == null) {
            throw new BusinessException("No default price list is configured for store id " + storeId + ".");
        }

        Optional<ProductPrice> productPrice = productPriceRepository.findByPriceListIdAndProductId(priceListId, productId);
        if (productPrice.isEmpty()) {
            return Optional.empty();
        }

        ProductPrice price = productPrice.get();
        if (!price.isActive()) {
            return Optional.empty();
        }
        if (price.getEffectiveFrom() != null && asOfDate.isBefore(price.getEffectiveFrom())) {
            return Optional.empty();
        }
        if (price.getEffectiveTo() != null && asOfDate.isAfter(price.getEffectiveTo())) {
            return Optional.empty();
        }

        return Optional.of(new ResolvedPrice(priceListId, price.getBasePrice(), price.getSellingPrice()));
    }
}
