package com.retailpos.customer.repository;

import com.retailpos.customer.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByOrganizationIdAndCode(Long organizationId, String code);

    Page<Customer> findAllByOrganizationId(Long organizationId, Pageable pageable);

    /** Mobile isn't unique (unlike sku/barcode on Product), so this always returns a List. */
    List<Customer> findAllByOrganizationIdAndMobile(Long organizationId, String mobile);
}
