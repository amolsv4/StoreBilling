package com.retailpos.product.controller;

import com.retailpos.common.dto.ApiResponse;
import com.retailpos.product.dto.CreateProductRequest;
import com.retailpos.product.dto.ProductResponse;
import com.retailpos.product.dto.UpdateProductRequest;
import com.retailpos.product.service.ProductService;
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
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCT_VIEW')")
    public ApiResponse<Page<ProductResponse>> list(@RequestParam(required = false) Long organizationId,
                                                      @RequestParam(required = false) Long categoryId,
                                                      @RequestParam(required = false) String search,
                                                      Pageable pageable) {
        return ApiResponse.success(productService.list(organizationId, categoryId, search, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_VIEW')")
    public ApiResponse<ProductResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(productService.getById(id));
    }

    /** Exact sku/barcode match for barcode-scan speed - a single result, not a page. */
    @GetMapping("/lookup")
    @PreAuthorize("hasAuthority('PRODUCT_VIEW')")
    public ApiResponse<ProductResponse> lookup(@RequestParam Long organizationId, @RequestParam String code) {
        return ApiResponse.success(productService.lookup(organizationId, code));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    public ApiResponse<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        return ApiResponse.success("Product created.", productService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    public ApiResponse<ProductResponse> update(@PathVariable Long id,
                                                 @Valid @RequestBody UpdateProductRequest request) {
        return ApiResponse.success("Product updated.", productService.update(id, request));
    }
}
