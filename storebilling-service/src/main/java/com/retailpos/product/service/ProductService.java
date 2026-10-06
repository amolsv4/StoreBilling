package com.retailpos.product.service;

import com.retailpos.product.dto.CreateProductRequest;
import com.retailpos.product.dto.ProductResponse;
import com.retailpos.product.dto.UpdateProductRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {

    ProductResponse create(CreateProductRequest request);

    ProductResponse update(Long id, UpdateProductRequest request);

    ProductResponse getById(Long id);

    Page<ProductResponse> list(Long organizationId, Long categoryId, String search, Pageable pageable);

    /**
     * Exact sku/barcode match for barcode-scan speed. Throws {@link com.retailpos.common.exception.ResourceNotFoundException}
     * if none match; if more than one matches (a sku colliding with a different product's
     * barcode), the first result is returned.
     */
    ProductResponse lookup(Long organizationId, String code);
}
