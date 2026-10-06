package com.retailpos.category.service;

import com.retailpos.category.dto.CategoryResponse;
import com.retailpos.category.dto.CreateCategoryRequest;
import com.retailpos.category.dto.UpdateCategoryRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {

    CategoryResponse create(CreateCategoryRequest request);

    CategoryResponse update(Long id, UpdateCategoryRequest request);

    CategoryResponse getById(Long id);

    Page<CategoryResponse> list(Long organizationId, Pageable pageable);
}
