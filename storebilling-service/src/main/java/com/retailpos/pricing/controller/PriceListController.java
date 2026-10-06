package com.retailpos.pricing.controller;

import com.retailpos.common.dto.ApiResponse;
import com.retailpos.pricing.dto.CreatePriceListRequest;
import com.retailpos.pricing.dto.PriceListResponse;
import com.retailpos.pricing.dto.UpdatePriceListRequest;
import com.retailpos.pricing.service.PriceListService;
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
@RequestMapping("/api/v1/price-lists")
public class PriceListController {

    private final PriceListService priceListService;

    public PriceListController(PriceListService priceListService) {
        this.priceListService = priceListService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PRICING_VIEW')")
    public ApiResponse<Page<PriceListResponse>> list(@RequestParam(required = false) Long organizationId,
                                                        Pageable pageable) {
        return ApiResponse.success(priceListService.list(organizationId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRICING_VIEW')")
    public ApiResponse<PriceListResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(priceListService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PRICING_CREATE')")
    public ApiResponse<PriceListResponse> create(@Valid @RequestBody CreatePriceListRequest request) {
        return ApiResponse.success("Price list created.", priceListService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRICING_UPDATE')")
    public ApiResponse<PriceListResponse> update(@PathVariable Long id,
                                                    @Valid @RequestBody UpdatePriceListRequest request) {
        return ApiResponse.success("Price list updated.", priceListService.update(id, request));
    }
}
