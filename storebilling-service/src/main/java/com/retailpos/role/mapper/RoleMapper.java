package com.retailpos.role.mapper;

import com.retailpos.role.dto.CreateRoleRequest;
import com.retailpos.role.dto.PermissionResponse;
import com.retailpos.role.dto.RoleResponse;
import com.retailpos.role.dto.UpdateRoleRequest;
import com.retailpos.role.entity.Permission;
import com.retailpos.role.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper {

    Role toEntity(CreateRoleRequest request);

    @Mapping(target = "permissions", ignore = true)
    RoleResponse toResponse(Role entity);

    void updateEntityFromRequest(UpdateRoleRequest request, @MappingTarget Role entity);

    PermissionResponse toPermissionResponse(Permission permission);

    List<PermissionResponse> toPermissionResponses(List<Permission> permissions);
}
