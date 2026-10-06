package com.retailpos.tax.repository;

import com.retailpos.tax.entity.TaxGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxGroupRepository extends JpaRepository<TaxGroup, Long> {

    boolean existsByOrganizationIdAndCode(Long organizationId, String code);

    Page<TaxGroup> findAllByOrganizationId(Long organizationId, Pageable pageable);
}
