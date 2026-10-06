package com.retailpos.organization.controller;

import com.retailpos.common.dto.ApiResponse;
import com.retailpos.organization.dto.CreateOrganizationRequest;
import com.retailpos.organization.dto.OrganizationResponse;
import com.retailpos.organization.dto.UpdateOrganizationRequest;
import com.retailpos.organization.service.OrganizationService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * No DELETE endpoint - soft delete only, via {@code PUT} with {@code active=false}.
 */
@RestController
@RequestMapping("/api/v1/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ORGANIZATION_VIEW')")
    public ApiResponse<Page<OrganizationResponse>> list(Pageable pageable) {
        return ApiResponse.success(organizationService.list(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ORGANIZATION_VIEW')")
    public ApiResponse<OrganizationResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(organizationService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ORGANIZATION_CREATE')")
    public ApiResponse<OrganizationResponse> create(@Valid @RequestBody CreateOrganizationRequest request) {
        return ApiResponse.success("Organization created.", organizationService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ORGANIZATION_UPDATE')")
    public ApiResponse<OrganizationResponse> update(@PathVariable Long id,
                                                      @Valid @RequestBody UpdateOrganizationRequest request) {
        return ApiResponse.success("Organization updated.", organizationService.update(id, request));
    }
}
