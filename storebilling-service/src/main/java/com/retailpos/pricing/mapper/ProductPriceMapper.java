package com.retailpos.pricing.mapper;

import com.retailpos.pricing.dto.CreateProductPriceRequest;
import com.retailpos.pricing.dto.ProductPriceResponse;
import com.retailpos.pricing.dto.UpdateProductPriceRequest;
import com.retailpos.pricing.entity.ProductPrice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductPriceMapper {

    @Mapping(target = "priceList", ignore = true)
    @Mapping(target = "product", ignore = true)
    ProductPrice toEntity(CreateProductPriceRequest request);

    @Mapping(target = "priceListId", source = "priceList.id")
    @Mapping(target = "priceListCode", source = "priceList.code")
    @Mapping(target = "priceListName", source = "priceList.name")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productSku", source = "product.sku")
    @Mapping(target = "productName", source = "product.name")
    ProductPriceResponse toResponse(ProductPrice entity);

    /** "active" is intentionally excluded - the service sets it explicitly when non-null. */
    @Mapping(target = "active", ignore = true)
    void updateEntityFromRequest(UpdateProductPriceRequest request, @MappingTarget ProductPrice entity);
}
