package com.retailpos.customer.service;

import com.retailpos.common.exception.DuplicateResourceException;
import com.retailpos.common.exception.ResourceNotFoundException;
import com.retailpos.customer.dto.CreateCustomerRequest;
import com.retailpos.customer.dto.CustomerResponse;
import com.retailpos.customer.dto.UpdateCustomerRequest;
import com.retailpos.customer.entity.Customer;
import com.retailpos.customer.mapper.CustomerMapper;
import com.retailpos.customer.repository.CustomerRepository;
import com.retailpos.organization.entity.Organization;
import com.retailpos.organization.repository.OrganizationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final OrganizationRepository organizationRepository;
    private final CustomerMapper customerMapper;

    public CustomerServiceImpl(CustomerRepository customerRepository,
                                OrganizationRepository organizationRepository,
                                CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.organizationRepository = organizationRepository;
        this.customerMapper = customerMapper;
    }

    @Override
    @Transactional
    public CustomerResponse create(CreateCustomerRequest request) {
        if (customerRepository.existsByOrganizationIdAndCode(request.organizationId(), request.code())) {
            throw new DuplicateResourceException(
                    "A customer with code '" + request.code() + "' already exists for this organization.");
        }
        Organization organization = organizationRepository.findById(request.organizationId())
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Organization", request.organizationId()));

        Customer customer = customerMapper.toEntity(request);
        customer.setOrganization(organization);
        customer.setCreditLimit(request.creditLimit() != null ? request.creditLimit() : BigDecimal.ZERO);
        Customer saved = customerRepository.save(customer);
        return customerMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CustomerResponse update(Long id, UpdateCustomerRequest request) {
        Customer customer = getEntity(id);
        customerMapper.updateEntityFromRequest(request, customer);
        if (request.creditLimit() != null) {
            customer.setCreditLimit(request.creditLimit());
        }
        if (request.active() != null) {
            customer.setActive(request.active());
        }
        Customer saved = customerRepository.save(customer);
        return customerMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getById(Long id) {
        return customerMapper.toResponse(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> list(Long organizationId, Pageable pageable) {
        if (organizationId != null) {
            return customerRepository.findAllByOrganizationId(organizationId, pageable).map(customerMapper::toResponse);
        }
        return customerRepository.findAll(pageable).map(customerMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> lookupByMobile(Long organizationId, String mobile) {
        return customerRepository.findAllByOrganizationIdAndMobile(organizationId, mobile).stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    private Customer getEntity(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Customer", id));
    }
}
