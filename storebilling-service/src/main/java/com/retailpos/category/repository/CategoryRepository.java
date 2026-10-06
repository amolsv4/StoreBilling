package com.retailpos.category.repository;

import com.retailpos.category.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByOrganizationIdAndCode(Long organizationId, String code);

    Page<Category> findAllByOrganizationId(Long organizationId, Pageable pageable);
}
