package com.retailpos.tax.repository;

import com.retailpos.tax.entity.TaxComponent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaxComponentRepository extends JpaRepository<TaxComponent, Long> {

    boolean existsByOrganizationIdAndCode(Long organizationId, String code);

    Page<TaxComponent> findAllByOrganizationId(Long organizationId, Pageable pageable);
}
