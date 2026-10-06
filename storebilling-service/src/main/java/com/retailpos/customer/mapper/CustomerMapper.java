package com.retailpos.customer.mapper;

import com.retailpos.customer.dto.CreateCustomerRequest;
import com.retailpos.customer.dto.CustomerResponse;
import com.retailpos.customer.dto.UpdateCustomerRequest;
import com.retailpos.customer.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CustomerMapper {

    /**
     * "creditLimit" is intentionally excluded - the column is NOT NULL with a zero default, but
     * the request field is optional, so the service resolves the fallback and sets it explicitly.
     */
    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "creditLimit", ignore = true)
    Customer toEntity(CreateCustomerRequest request);

    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "organizationCode", source = "organization.code")
    @Mapping(target = "organizationName", source = "organization.name")
    CustomerResponse toResponse(Customer entity);

    /**
     * "active" and "creditLimit" are intentionally excluded - the service sets both explicitly
     * only when the corresponding request field is non-null, preserving the existing value
     * otherwise (rather than risk clearing a NOT NULL column to null).
     */
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "creditLimit", ignore = true)
    void updateEntityFromRequest(UpdateCustomerRequest request, @MappingTarget Customer entity);
}
