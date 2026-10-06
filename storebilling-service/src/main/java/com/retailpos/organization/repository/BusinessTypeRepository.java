package com.retailpos.organization.repository;

import com.retailpos.organization.entity.BusinessType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BusinessTypeRepository extends JpaRepository<BusinessType, Long> {

    Optional<BusinessType> findByCode(String code);

    boolean existsByCode(String code);
}
