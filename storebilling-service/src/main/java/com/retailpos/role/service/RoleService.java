package com.retailpos.role.service;

import com.retailpos.role.dto.CreateRoleRequest;
import com.retailpos.role.dto.RoleResponse;
import com.retailpos.role.dto.UpdateRoleRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RoleService {

    RoleResponse create(CreateRoleRequest request);

    RoleResponse update(Long id, UpdateRoleRequest request);

    RoleResponse getById(Long id);

    Page<RoleResponse> list(Pageable pageable);
}
