package com.retailpos.customer.service;

import com.retailpos.customer.dto.CreateCustomerRequest;
import com.retailpos.customer.dto.CustomerResponse;
import com.retailpos.customer.dto.UpdateCustomerRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomerService {

    CustomerResponse create(CreateCustomerRequest request);

    CustomerResponse update(Long id, UpdateCustomerRequest request);

    CustomerResponse getById(Long id);

    Page<CustomerResponse> list(Long organizationId, Pageable pageable);

    /** Mobile-number lookup (mobile isn't unique) - used by POS customer search. */
    List<CustomerResponse> lookupByMobile(Long organizationId, String mobile);
}
