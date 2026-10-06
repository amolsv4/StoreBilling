package com.retailpos.tax.mapper;

import com.retailpos.tax.dto.CreateTaxComponentRequest;
import com.retailpos.tax.dto.TaxComponentResponse;
import com.retailpos.tax.dto.UpdateTaxComponentRequest;
import com.retailpos.tax.entity.TaxComponent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaxComponentMapper {

    @Mapping(target = "organization", ignore = true)
    TaxComponent toEntity(CreateTaxComponentRequest request);

    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "organizationCode", source = "organization.code")
    @Mapping(target = "organizationName", source = "organization.name")
    TaxComponentResponse toResponse(TaxComponent entity);

    /** "active" is intentionally excluded - the service sets it explicitly when non-null. */
    @Mapping(target = "active", ignore = true)
    void updateEntityFromRequest(UpdateTaxComponentRequest request, @MappingTarget TaxComponent entity);
}
