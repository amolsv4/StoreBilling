package com.retailpos.store.mapper;

import com.retailpos.store.dto.CreateStoreRequest;
import com.retailpos.store.dto.StoreResponse;
import com.retailpos.store.dto.UpdateStoreRequest;
import com.retailpos.store.entity.Store;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StoreMapper {

    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "businessType", ignore = true)
    Store toEntity(CreateStoreRequest request);

    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "organizationCode", source = "organization.code")
    @Mapping(target = "businessTypeId", source = "businessType.id")
    @Mapping(target = "businessTypeCode", source = "businessType.code")
    StoreResponse toResponse(Store entity);

    /**
     * "active"/"defaultPriceListId" are intentionally excluded - the service sets both explicitly
     * only when the corresponding request field is non-null (defaultPriceListId additionally
     * requires a same-organization existence check before it can be set).
     */
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "defaultPriceListId", ignore = true)
    void updateEntityFromRequest(UpdateStoreRequest request, @MappingTarget Store entity);
}
