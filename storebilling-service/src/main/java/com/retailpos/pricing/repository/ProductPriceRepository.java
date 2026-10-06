package com.retailpos.pricing.repository;

import com.retailpos.pricing.entity.ProductPrice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductPriceRepository extends JpaRepository<ProductPrice, Long> {

    /**
     * Single-row fetch used by {@code PricingResolutionService} - safe because
     * {@code (price_list_id, product_id)} is already DB-unique (see {@code uk_product_price}).
     */
    Optional<ProductPrice> findByPriceListIdAndProductId(Long priceListId, Long productId);

    /**
     * Mirrors the DB's {@code UNIQUE(price_list_id, product_id)} constraint exactly (the
     * constraint has no active-only filter, so a previously soft-deactivated row for the same
     * pair still occupies the key) - used to turn a would-be constraint violation into a clean
     * {@link com.retailpos.common.exception.DuplicateResourceException} (409) before it ever
     * reaches the database.
     */
    boolean existsByPriceListIdAndProductId(Long priceListId, Long productId);

    Page<ProductPrice> findAllByPriceListId(Long priceListId, Pageable pageable);

    Page<ProductPrice> findAllByProductId(Long productId, Pageable pageable);

    Page<ProductPrice> findAllByPriceListIdAndProductId(Long priceListId, Long productId, Pageable pageable);
}
