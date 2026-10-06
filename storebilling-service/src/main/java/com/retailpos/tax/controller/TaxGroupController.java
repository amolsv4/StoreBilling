package com.retailpos.tax.controller;

import com.retailpos.common.dto.ApiResponse;
import com.retailpos.tax.dto.CreateTaxGroupRequest;
import com.retailpos.tax.dto.TaxGroupResponse;
import com.retailpos.tax.dto.UpdateTaxGroupRequest;
import com.retailpos.tax.service.TaxGroupService;
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
@RequestMapping("/api/v1/tax-groups")
public class TaxGroupController {

    private final TaxGroupService taxGroupService;

    public TaxGroupController(TaxGroupService taxGroupService) {
        this.taxGroupService = taxGroupService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('TAX_VIEW')")
    public ApiResponse<Page<TaxGroupResponse>> list(@RequestParam(required = false) Long organizationId,
                                                      Pageable pageable) {
        return ApiResponse.success(taxGroupService.list(organizationId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TAX_VIEW')")
    public ApiResponse<TaxGroupResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(taxGroupService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('TAX_CREATE')")
    public ApiResponse<TaxGroupResponse> create(@Valid @RequestBody CreateTaxGroupRequest request) {
        return ApiResponse.success("Tax group created.", taxGroupService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TAX_UPDATE')")
    public ApiResponse<TaxGroupResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody UpdateTaxGroupRequest request) {
        return ApiResponse.success("Tax group updated.", taxGroupService.update(id, request));
    }
}
