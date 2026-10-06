package com.retailpos.organization.mapper;

import com.retailpos.organization.dto.CreateOrganizationRequest;
import com.retailpos.organization.dto.OrganizationResponse;
import com.retailpos.organization.dto.UpdateOrganizationRequest;
import com.retailpos.organization.entity.Organization;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrganizationMapper {

    Organization toEntity(CreateOrganizationRequest request);

    OrganizationResponse toResponse(Organization entity);

    /** Note: "active" is intentionally excluded here - the service sets it explicitly when non-null. */
    @Mapping(target = "active", ignore = true)
    void updateEntityFromRequest(UpdateOrganizationRequest request, @MappingTarget Organization entity);
}
