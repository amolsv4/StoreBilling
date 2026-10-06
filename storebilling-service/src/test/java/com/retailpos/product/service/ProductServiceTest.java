package com.retailpos.product.service;

import com.retailpos.category.entity.Category;
import com.retailpos.category.repository.CategoryRepository;
import com.retailpos.common.exception.BusinessException;
import com.retailpos.organization.entity.Organization;
import com.retailpos.organization.repository.OrganizationRepository;
import com.retailpos.product.dto.CreateProductRequest;
import com.retailpos.product.entity.UnitOfMeasure;
import com.retailpos.product.mapper.ProductMapper;
import com.retailpos.product.repository.ProductRepository;
import com.retailpos.tax.entity.TaxGroup;
import com.retailpos.tax.repository.TaxGroupRepository;
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
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private TaxGroupRepository taxGroupRepository;
    @Mock
    private ProductMapper productMapper;

    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(productRepository, organizationRepository, categoryRepository,
                taxGroupRepository, productMapper);
    }

    private Organization organization(Long id) {
        Organization organization = new Organization();
        organization.setId(id);
        return organization;
    }

    @Test
    void createRejectsCategoryFromADifferentOrganization() {
        CreateProductRequest request = new CreateProductRequest(
                100L, 5L, null, "SKU-1", null, "Demo Product", null, null, UnitOfMeasure.PCS, null, null);

        when(productRepository.existsByOrganizationIdAndSku(100L, "SKU-1")).thenReturn(false);
        lenient().when(organizationRepository.findById(100L)).thenReturn(Optional.of(organization(100L)));

        Category categoryFromOtherOrg = new Category();
        categoryFromOtherOrg.setId(5L);
        categoryFromOtherOrg.setCode("CAT-OTHER");
        categoryFromOtherOrg.setOrganization(organization(999L));
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(categoryFromOtherOrg));

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> productService.create(request))
                .withMessageContaining("different organization");

        verify(productRepository, never()).save(any());
    }

    @Test
    void createRejectsTaxGroupFromADifferentOrganization() {
        CreateProductRequest request = new CreateProductRequest(
                100L, 5L, 7L, "SKU-1", null, "Demo Product", null, null, UnitOfMeasure.PCS, null, null);

        when(productRepository.existsByOrganizationIdAndSku(100L, "SKU-1")).thenReturn(false);
        lenient().when(organizationRepository.findById(100L)).thenReturn(Optional.of(organization(100L)));

        Category categorySameOrg = new Category();
        categorySameOrg.setId(5L);
        categorySameOrg.setCode("CAT-SAME");
        categorySameOrg.setOrganization(organization(100L));
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(categorySameOrg));

        TaxGroup taxGroupFromOtherOrg = new TaxGroup();
        taxGroupFromOtherOrg.setId(7L);
        taxGroupFromOtherOrg.setCode("TAXGRP-OTHER");
        taxGroupFromOtherOrg.setOrganization(organization(999L));
        when(taxGroupRepository.findById(7L)).thenReturn(Optional.of(taxGroupFromOtherOrg));

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> productService.create(request))
                .withMessageContaining("different organization");

        verify(productRepository, never()).save(any());
    }
}
