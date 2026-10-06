package com.retailpos.pricing.service;

import com.retailpos.pricing.dto.CreateProductPriceRequest;
import com.retailpos.pricing.dto.ProductPriceResponse;
import com.retailpos.pricing.dto.UpdateProductPriceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductPriceService {

    ProductPriceResponse create(CreateProductPriceRequest request);

    ProductPriceResponse update(Long id, UpdateProductPriceRequest request);

    ProductPriceResponse getById(Long id);

    Page<ProductPriceResponse> list(Long priceListId, Long productId, Pageable pageable);
}
