package com.retailpos.organization.controller;

import com.retailpos.common.dto.ApiResponse;
import com.retailpos.organization.dto.BusinessTypeResponse;
import com.retailpos.organization.service.BusinessTypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only: business types are global reference rows seeded via Flyway, not user-managed CRUD.
 * Any authenticated user may list them (no dedicated BUSINESS_TYPE_* permission exists, since this
 * is static reference data rather than a permissioned module).
 */
@RestController
@RequestMapping("/api/v1/business-types")
public class BusinessTypeController {

    private final BusinessTypeService businessTypeService;

    public BusinessTypeController(BusinessTypeService businessTypeService) {
        this.businessTypeService = businessTypeService;
    }

    @GetMapping
    public ApiResponse<List<BusinessTypeResponse>> list() {
        return ApiResponse.success(businessTypeService.listAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<BusinessTypeResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(businessTypeService.getById(id));
    }
}
