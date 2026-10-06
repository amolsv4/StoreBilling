package com.retailpos.organization.mapper;

import com.retailpos.organization.dto.BusinessTypeResponse;
import com.retailpos.organization.entity.BusinessType;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BusinessTypeMapper {

    BusinessTypeResponse toResponse(BusinessType entity);
}
