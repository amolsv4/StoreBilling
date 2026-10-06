package com.retailpos.pricing.repository;

import com.retailpos.pricing.entity.PriceList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriceListRepository extends JpaRepository<PriceList, Long> {

    boolean existsByOrganizationIdAndCode(Long organizationId, String code);

    Page<PriceList> findAllByOrganizationId(Long organizationId, Pageable pageable);
}
