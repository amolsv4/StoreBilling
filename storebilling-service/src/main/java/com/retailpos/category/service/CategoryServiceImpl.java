package com.retailpos.category.service;

import com.retailpos.category.dto.CategoryResponse;
import com.retailpos.category.dto.CreateCategoryRequest;
import com.retailpos.category.dto.UpdateCategoryRequest;
import com.retailpos.category.entity.Category;
import com.retailpos.category.mapper.CategoryMapper;
import com.retailpos.category.repository.CategoryRepository;
import com.retailpos.common.exception.BusinessException;
import com.retailpos.common.exception.DuplicateResourceException;
import com.retailpos.common.exception.ResourceNotFoundException;
import com.retailpos.organization.entity.Organization;
import com.retailpos.organization.repository.OrganizationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryServiceImpl implements CategoryService {

    private static final int MAX_HIERARCHY_DEPTH = 20;

    private final CategoryRepository categoryRepository;
    private final OrganizationRepository organizationRepository;
    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository,
                                OrganizationRepository organizationRepository,
                                CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.organizationRepository = organizationRepository;
        this.categoryMapper = categoryMapper;
    }

    @Override
    @Transactional
    public CategoryResponse create(CreateCategoryRequest request) {
        if (categoryRepository.existsByOrganizationIdAndCode(request.organizationId(), request.code())) {
            throw new DuplicateResourceException(
                    "A category with code '" + request.code() + "' already exists for this organization.");
        }
        Organization organization = organizationRepository.findById(request.organizationId())
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Organization", request.organizationId()));

        validateParentAssignment(null, request.parentCategoryId(), request.organizationId());

        Category category = categoryMapper.toEntity(request);
        category.setOrganization(organization);
        if (request.parentCategoryId() != null) {
            category.setParentCategory(getEntity(request.parentCategoryId()));
        }
        Category saved = categoryRepository.save(category);
        return categoryMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CategoryResponse update(Long id, UpdateCategoryRequest request) {
        Category category = getEntity(id);
        Long organizationId = category.getOrganization().getId();

        validateParentAssignment(id, request.parentCategoryId(), organizationId);

        categoryMapper.updateEntityFromRequest(request, category);
        category.setParentCategory(request.parentCategoryId() != null ? getEntity(request.parentCategoryId()) : null);
        if (request.active() != null) {
            category.setActive(request.active());
        }
        Category saved = categoryRepository.save(category);
        return categoryMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(Long id) {
        return categoryMapper.toResponse(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CategoryResponse> list(Long organizationId, Pageable pageable) {
        if (organizationId != null) {
            return categoryRepository.findAllByOrganizationId(organizationId, pageable).map(categoryMapper::toResponse);
        }
        return categoryRepository.findAll(pageable).map(categoryMapper::toResponse);
    }

    /**
     * Rejects a parent assignment that would make a category its own parent, belong to a
     * different organization, or introduce a cycle anywhere up the ancestor chain. Called with
     * {@code categoryId=null} from {@link #create} (a brand-new category can never be its own
     * ancestor) and with the real id from {@link #update}.
     */
    private void validateParentAssignment(Long categoryId, Long candidateParentId, Long organizationId) {
        if (candidateParentId == null) {
            return;
        }
        if (candidateParentId.equals(categoryId)) {
            throw new BusinessException("A category cannot be its own parent.");
        }
        Category candidate = categoryRepository.findById(candidateParentId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Category", candidateParentId));
        if (!candidate.getOrganization().getId().equals(organizationId)) {
            throw new BusinessException("Parent category must belong to the same organization.");
        }
        Long currentId = candidate.getParentCategory() != null ? candidate.getParentCategory().getId() : null;
        int depth = 0;
        while (currentId != null) {
            if (currentId.equals(categoryId)) {
                throw new BusinessException("This parent assignment would create a category cycle.");
            }
            if (++depth > MAX_HIERARCHY_DEPTH) {
                throw new BusinessException("Category hierarchy exceeds the maximum allowed depth.");
            }
            Category ancestor = categoryRepository.findById(currentId).orElse(null);
            currentId = (ancestor != null && ancestor.getParentCategory() != null)
                    ? ancestor.getParentCategory().getId()
                    : null;
        }
    }

    private Category getEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Category", id));
    }
}
