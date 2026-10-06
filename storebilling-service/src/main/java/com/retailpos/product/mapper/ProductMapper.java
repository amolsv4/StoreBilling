package com.retailpos.product.mapper;

import com.retailpos.product.dto.CreateProductRequest;
import com.retailpos.product.dto.ProductResponse;
import com.retailpos.product.dto.UpdateProductRequest;
import com.retailpos.product.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "taxGroup", ignore = true)
    Product toEntity(CreateProductRequest request);

    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "organizationCode", source = "organization.code")
    @Mapping(target = "organizationName", source = "organization.name")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryCode", source = "category.code")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "taxGroupId", source = "taxGroup.id")
    @Mapping(target = "taxGroupCode", source = "taxGroup.code")
    @Mapping(target = "taxGroupName", source = "taxGroup.name")
    ProductResponse toResponse(Product entity);

    /**
     * "active"/"category"/"taxGroup" are intentionally excluded - the service resolves, validates
     * (same-organization check) and sets all three explicitly. "saleEnabled"/"purchaseEnabled"
     * default to true only on create (handled in the service), so they are mapped normally here.
     */
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "taxGroup", ignore = true)
    void updateEntityFromRequest(UpdateProductRequest request, @MappingTarget Product entity);
}
