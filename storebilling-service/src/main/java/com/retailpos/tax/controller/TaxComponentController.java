package com.retailpos.tax.controller;

import com.retailpos.common.dto.ApiResponse;
import com.retailpos.tax.dto.CreateTaxComponentRequest;
import com.retailpos.tax.dto.TaxComponentResponse;
import com.retailpos.tax.dto.UpdateTaxComponentRequest;
import com.retailpos.tax.service.TaxComponentService;
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
@RequestMapping("/api/v1/tax-components")
public class TaxComponentController {

    private final TaxComponentService taxComponentService;

    public TaxComponentController(TaxComponentService taxComponentService) {
        this.taxComponentService = taxComponentService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('TAX_VIEW')")
    public ApiResponse<Page<TaxComponentResponse>> list(@RequestParam(required = false) Long organizationId,
                                                          Pageable pageable) {
        return ApiResponse.success(taxComponentService.list(organizationId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TAX_VIEW')")
    public ApiResponse<TaxComponentResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(taxComponentService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('TAX_CREATE')")
    public ApiResponse<TaxComponentResponse> create(@Valid @RequestBody CreateTaxComponentRequest request) {
        return ApiResponse.success("Tax component created.", taxComponentService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TAX_UPDATE')")
    public ApiResponse<TaxComponentResponse> update(@PathVariable Long id,
                                                       @Valid @RequestBody UpdateTaxComponentRequest request) {
        return ApiResponse.success("Tax component updated.", taxComponentService.update(id, request));
    }
}
