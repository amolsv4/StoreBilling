package com.retailpos.product.service;

import com.retailpos.category.entity.Category;
import com.retailpos.category.repository.CategoryRepository;
import com.retailpos.common.exception.BusinessException;
import com.retailpos.common.exception.DuplicateResourceException;
import com.retailpos.common.exception.ResourceNotFoundException;
import com.retailpos.organization.entity.Organization;
import com.retailpos.organization.repository.OrganizationRepository;
import com.retailpos.product.dto.CreateProductRequest;
import com.retailpos.product.dto.ProductResponse;
import com.retailpos.product.dto.UpdateProductRequest;
import com.retailpos.product.entity.Product;
import com.retailpos.product.mapper.ProductMapper;
import com.retailpos.product.repository.ProductRepository;
import com.retailpos.product.repository.ProductSpecifications;
import com.retailpos.tax.entity.TaxGroup;
import com.retailpos.tax.repository.TaxGroupRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final OrganizationRepository organizationRepository;
    private final CategoryRepository categoryRepository;
    private final TaxGroupRepository taxGroupRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductRepository productRepository,
                               OrganizationRepository organizationRepository,
                               CategoryRepository categoryRepository,
                               TaxGroupRepository taxGroupRepository,
                               ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.organizationRepository = organizationRepository;
        this.categoryRepository = categoryRepository;
        this.taxGroupRepository = taxGroupRepository;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        if (productRepository.existsByOrganizationIdAndSku(request.organizationId(), request.sku())) {
            throw new DuplicateResourceException(
                    "A product with SKU '" + request.sku() + "' already exists for this organization.");
        }
        if (request.barcode() != null
                && productRepository.existsByOrganizationIdAndBarcode(request.organizationId(), request.barcode())) {
            throw new DuplicateResourceException(
                    "A product with barcode '" + request.barcode() + "' already exists for this organization.");
        }
        Organization organization = organizationRepository.findById(request.organizationId())
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Organization", request.organizationId()));

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Category", request.categoryId()));
        if (!category.getOrganization().getId().equals(request.organizationId())) {
            throw new BusinessException("Category '" + category.getCode() + "' belongs to a different organization.");
        }

        TaxGroup taxGroup = null;
        if (request.taxGroupId() != null) {
            taxGroup = taxGroupRepository.findById(request.taxGroupId())
                    .orElseThrow(() -> ResourceNotFoundException.forEntity("TaxGroup", request.taxGroupId()));
            if (!taxGroup.getOrganization().getId().equals(request.organizationId())) {
                throw new BusinessException("Tax group '" + taxGroup.getCode() + "' belongs to a different organization.");
            }
        }

        Product product = productMapper.toEntity(request);
        product.setOrganization(organization);
        product.setCategory(category);
        product.setTaxGroup(taxGroup);
        product.setSaleEnabled(request.saleEnabled() == null || request.saleEnabled());
        product.setPurchaseEnabled(request.purchaseEnabled() == null || request.purchaseEnabled());
        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, UpdateProductRequest request) {
        Product product = getEntity(id);
        Long organizationId = product.getOrganization().getId();

        if (request.barcode() != null
                && !request.barcode().equals(product.getBarcode())
                && productRepository.existsByOrganizationIdAndBarcode(organizationId, request.barcode())) {
            throw new DuplicateResourceException(
                    "A product with barcode '" + request.barcode() + "' already exists for this organization.");
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Category", request.categoryId()));
        if (!category.getOrganization().getId().equals(organizationId)) {
            throw new BusinessException("Category '" + category.getCode() + "' belongs to a different organization.");
        }

        TaxGroup taxGroup = null;
        if (request.taxGroupId() != null) {
            taxGroup = taxGroupRepository.findById(request.taxGroupId())
                    .orElseThrow(() -> ResourceNotFoundException.forEntity("TaxGroup", request.taxGroupId()));
            if (!taxGroup.getOrganization().getId().equals(organizationId)) {
                throw new BusinessException("Tax group '" + taxGroup.getCode() + "' belongs to a different organization.");
            }
        }

        productMapper.updateEntityFromRequest(request, product);
        product.setCategory(category);
        product.setTaxGroup(taxGroup);
        if (request.active() != null) {
            product.setActive(request.active());
        }
        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return productMapper.toResponse(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> list(Long organizationId, Long categoryId, String search, Pageable pageable) {
        Page<Product> page;
        if (search != null && !search.isBlank()) {
            // The fuzzy "search" param always goes through the Specification path (mirrors
            // AppConfigurationSpecifications' style) - the plain equality finders above remain in
            // place for the common no-search case and are still exercised by ProductServiceTest.
            page = productRepository.findAll(ProductSpecifications.search(organizationId, categoryId, search), pageable);
        } else if (organizationId != null && categoryId != null) {
            page = productRepository.findAllByOrganizationIdAndCategoryId(organizationId, categoryId, pageable);
        } else if (organizationId != null) {
            page = productRepository.findAllByOrganizationId(organizationId, pageable);
        } else if (categoryId != null) {
            page = productRepository.findAllByCategoryId(categoryId, pageable);
        } else {
            page = productRepository.findAll(pageable);
        }
        return page.map(productMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse lookup(Long organizationId, String code) {
        List<Product> matches = productRepository.findByOrganizationIdAndSkuOrBarcode(organizationId, code);
        if (matches.isEmpty()) {
            throw ResourceNotFoundException.forEntity("Product", code);
        }
        return productMapper.toResponse(matches.get(0));
    }

    private Product getEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Product", id));
    }
}
