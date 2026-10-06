package com.retailpos.pricing.mapper;

import com.retailpos.pricing.dto.CreatePriceListRequest;
import com.retailpos.pricing.dto.PriceListResponse;
import com.retailpos.pricing.dto.UpdatePriceListRequest;
import com.retailpos.pricing.entity.PriceList;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PriceListMapper {

    @Mapping(target = "organization", ignore = true)
    PriceList toEntity(CreatePriceListRequest request);

    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "organizationCode", source = "organization.code")
    @Mapping(target = "organizationName", source = "organization.name")
    PriceListResponse toResponse(PriceList entity);

    /** "active" is intentionally excluded - the service sets it explicitly when non-null. */
    @Mapping(target = "active", ignore = true)
    void updateEntityFromRequest(UpdatePriceListRequest request, @MappingTarget PriceList entity);
}
