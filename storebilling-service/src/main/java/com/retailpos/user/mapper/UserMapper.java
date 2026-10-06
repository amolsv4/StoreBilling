package com.retailpos.user.mapper;

import com.retailpos.user.dto.CreateUserRequest;
import com.retailpos.user.dto.UpdateUserRequest;
import com.retailpos.user.dto.UserResponse;
import com.retailpos.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    User toEntity(CreateUserRequest request);

    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "roleCodes", ignore = true)
    @Mapping(target = "storeIds", ignore = true)
    UserResponse toResponse(User entity);

    @Mapping(target = "organization", ignore = true)
    void updateEntityFromRequest(UpdateUserRequest request, @MappingTarget User entity);
}
