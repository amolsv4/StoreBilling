package com.retailpos.pricing.controller;

import com.retailpos.common.dto.ApiResponse;
import com.retailpos.pricing.dto.CreateProductPriceRequest;
import com.retailpos.pricing.dto.ProductPriceResponse;
import com.retailpos.pricing.dto.UpdateProductPriceRequest;
import com.retailpos.pricing.service.ProductPriceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** No DELETE endpoint - soft delete only, via {@code PUT} with {@code active=false}. */
@RestController
@RequestMapping("/api/v1/product-prices")
public class ProductPriceController {

    private final ProductPriceService productPriceService;

    public ProductPriceController(ProductPriceService productPriceService) {
        this.productPriceService = productPriceService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PRICING_VIEW')")
    public ApiResponse<Page<ProductPriceResponse>> list(@RequestParam(required = false) Long priceListId,
                                                           @RequestParam(required = false) Long productId,
                                                           Pageable pageable) {
        return ApiResponse.success(productPriceService.list(priceListId, productId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRICING_VIEW')")
    public ApiResponse<ProductPriceResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(productPriceService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PRICING_CREATE')")
    public ApiResponse<ProductPriceResponse> create(@Valid @RequestBody CreateProductPriceRequest request) {
        return ApiResponse.success("Product price created.", productPriceService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRICING_UPDATE')")
    public ApiResponse<ProductPriceResponse> update(@PathVariable Long id,
                                                       @Valid @RequestBody UpdateProductPriceRequest request) {
        return ApiResponse.success("Product price updated.", productPriceService.update(id, request));
    }
}
