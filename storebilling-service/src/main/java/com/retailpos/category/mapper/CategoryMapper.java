package com.retailpos.category.mapper;

import com.retailpos.category.dto.CategoryResponse;
import com.retailpos.category.dto.CreateCategoryRequest;
import com.retailpos.category.dto.UpdateCategoryRequest;
import com.retailpos.category.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {

    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "parentCategory", ignore = true)
    Category toEntity(CreateCategoryRequest request);

    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "organizationCode", source = "organization.code")
    @Mapping(target = "organizationName", source = "organization.name")
    @Mapping(target = "parentCategoryId", source = "parentCategory.id")
    @Mapping(target = "parentCategoryCode", source = "parentCategory.code")
    @Mapping(target = "parentCategoryName", source = "parentCategory.name")
    CategoryResponse toResponse(Category entity);

    /**
     * "active" and "parentCategory" are intentionally excluded - the service resolves, validates
     * (cycle prevention) and sets both explicitly.
     */
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "parentCategory", ignore = true)
    void updateEntityFromRequest(UpdateCategoryRequest request, @MappingTarget Category entity);
}
