package com.retailpos.tax.mapper;

import com.retailpos.tax.dto.CreateTaxGroupRequest;
import com.retailpos.tax.dto.TaxGroupResponse;
import com.retailpos.tax.dto.UpdateTaxGroupRequest;
import com.retailpos.tax.entity.TaxGroup;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * "components"/"totalPercentage" are always excluded from the direct entity mapping - they are
 * assembled by the service (from the tax_group_component join rows), not derivable from the
 * {@link TaxGroup} entity alone.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaxGroupMapper {

    @Mapping(target = "organization", ignore = true)
    TaxGroup toEntity(CreateTaxGroupRequest request);

    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "organizationCode", source = "organization.code")
    @Mapping(target = "organizationName", source = "organization.name")
    @Mapping(target = "components", ignore = true)
    @Mapping(target = "totalPercentage", ignore = true)
    TaxGroupResponse toResponse(TaxGroup entity);

    /** "active"/"inclusive" are intentionally excluded - the service sets both explicitly when non-null. */
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "inclusive", ignore = true)
    void updateEntityFromRequest(UpdateTaxGroupRequest request, @MappingTarget TaxGroup entity);
}
