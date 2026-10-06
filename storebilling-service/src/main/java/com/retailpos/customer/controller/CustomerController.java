package com.retailpos.customer.controller;

import com.retailpos.common.dto.ApiResponse;
import com.retailpos.customer.dto.CreateCustomerRequest;
import com.retailpos.customer.dto.CustomerResponse;
import com.retailpos.customer.dto.UpdateCustomerRequest;
import com.retailpos.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.List;
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
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CUSTOMER_VIEW')")
    public ApiResponse<Page<CustomerResponse>> list(@RequestParam(required = false) Long organizationId,
                                                       Pageable pageable) {
        return ApiResponse.success(customerService.list(organizationId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER_VIEW')")
    public ApiResponse<CustomerResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(customerService.getById(id));
    }

    /** Mobile-number lookup (mobile isn't unique, so this may return several matches). */
    @GetMapping("/lookup")
    @PreAuthorize("hasAuthority('CUSTOMER_VIEW')")
    public ApiResponse<List<CustomerResponse>> lookup(@RequestParam Long organizationId,
                                                        @RequestParam String mobile) {
        return ApiResponse.success(customerService.lookupByMobile(organizationId, mobile));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('CUSTOMER_CREATE')")
    public ApiResponse<CustomerResponse> create(@Valid @RequestBody CreateCustomerRequest request) {
        return ApiResponse.success("Customer created.", customerService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER_UPDATE')")
    public ApiResponse<CustomerResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody UpdateCustomerRequest request) {
        return ApiResponse.success("Customer updated.", customerService.update(id, request));
    }
}
