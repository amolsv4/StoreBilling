package com.retailpos.category.controller;

import com.retailpos.category.dto.CategoryResponse;
import com.retailpos.category.dto.CreateCategoryRequest;
import com.retailpos.category.dto.UpdateCategoryRequest;
import com.retailpos.category.service.CategoryService;
import com.retailpos.common.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CATEGORY_VIEW')")
    public ApiResponse<Page<CategoryResponse>> list(@RequestParam(required = false) Long organizationId,
                                                       Pageable pageable) {
        return ApiResponse.success(categoryService.list(organizationId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CATEGORY_VIEW')")
    public ApiResponse<CategoryResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(categoryService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('CATEGORY_CREATE')")
    public ApiResponse<CategoryResponse> create(@Valid @RequestBody CreateCategoryRequest request) {
        return ApiResponse.success("Category created.", categoryService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CATEGORY_UPDATE')")
    public ApiResponse<CategoryResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody UpdateCategoryRequest request) {
        return ApiResponse.success("Category updated.", categoryService.update(id, request));
    }
}
