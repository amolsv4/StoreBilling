package com.retailpos.category.service;

import com.retailpos.category.dto.UpdateCategoryRequest;
import com.retailpos.category.entity.Category;
import com.retailpos.category.mapper.CategoryMapper;
import com.retailpos.category.repository.CategoryRepository;
import com.retailpos.common.exception.BusinessException;
import com.retailpos.organization.entity.Organization;
import com.retailpos.organization.repository.OrganizationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private CategoryMapper categoryMapper;

    private CategoryServiceImpl categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryServiceImpl(categoryRepository, organizationRepository, categoryMapper);
    }

    private Category category(Long id, Long organizationId, Category parent) {
        Category category = new Category();
        category.setId(id);
        Organization organization = new Organization();
        organization.setId(organizationId);
        category.setOrganization(organization);
        category.setParentCategory(parent);
        return category;
    }

    @Test
    void updateRejectsCategoryAsItsOwnParent() {
        Category self = category(1L, 100L, null);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(self));

        UpdateCategoryRequest request = new UpdateCategoryRequest("Renamed", null, 1L, null);

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> categoryService.update(1L, request))
                .withMessageContaining("cannot be its own parent");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void updateRejectsAssigningADescendantAsParent() {
        // A (id=1, no parent) is currently the parent of B (id=2). Attempting to re-parent A
        // under B would create a two-node cycle (A -> B -> A) and must be rejected.
        Category a = category(1L, 100L, null);
        Category b = category(2L, 100L, a);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(a));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(b));

        UpdateCategoryRequest request = new UpdateCategoryRequest("A renamed", null, 2L, null);

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> categoryService.update(1L, request))
                .withMessageContaining("cycle");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void updateRejectsParentFromADifferentOrganization() {
        Category self = category(1L, 100L, null);
        Category otherOrgParent = category(2L, 999L, null);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(self));
        lenient().when(categoryRepository.findById(2L)).thenReturn(Optional.of(otherOrgParent));

        UpdateCategoryRequest request = new UpdateCategoryRequest("Renamed", null, 2L, null);

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> categoryService.update(1L, request))
                .withMessageContaining("same organization");

        verify(categoryRepository, never()).save(any());
    }
}
