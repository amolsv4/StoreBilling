package com.retailpos.role.service;

import com.retailpos.common.exception.DuplicateResourceException;
import com.retailpos.common.exception.ResourceNotFoundException;
import com.retailpos.role.dto.CreateRoleRequest;
import com.retailpos.role.dto.RoleResponse;
import com.retailpos.role.dto.UpdateRoleRequest;
import com.retailpos.role.entity.Permission;
import com.retailpos.role.entity.Role;
import com.retailpos.role.entity.RolePermission;
import com.retailpos.role.mapper.RoleMapper;
import com.retailpos.role.repository.PermissionRepository;
import com.retailpos.role.repository.RolePermissionRepository;
import com.retailpos.role.repository.RoleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final RoleMapper roleMapper;

    public RoleServiceImpl(RoleRepository roleRepository, PermissionRepository permissionRepository,
                            RolePermissionRepository rolePermissionRepository, RoleMapper roleMapper) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.roleMapper = roleMapper;
    }

    @Override
    @Transactional
    public RoleResponse create(CreateRoleRequest request) {
        if (roleRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("A role with code '" + request.code() + "' already exists.");
        }
        Role role = roleMapper.toEntity(request);
        Role saved = roleRepository.save(role);
        assignPermissions(saved, request.permissionCodes());
        return toResponseWithPermissions(saved);
    }

    @Override
    @Transactional
    public RoleResponse update(Long id, UpdateRoleRequest request) {
        Role role = getEntity(id);
        roleMapper.updateEntityFromRequest(request, role);
        Role saved = roleRepository.save(role);
        assignPermissions(saved, request.permissionCodes());
        return toResponseWithPermissions(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getById(Long id) {
        return toResponseWithPermissions(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoleResponse> list(Pageable pageable) {
        return roleRepository.findAll(pageable).map(this::toResponseWithPermissions);
    }

    /**
     * Resyncs the role's permission set to exactly {@code permissionCodes}. role_permission is a
     * pure join/association table (not a primary domain entity), so replacing its rows on
     * assignment change - rather than trying to "soft delete" a many-to-many link - is the
     * standard, expected approach; it does not conflict with the soft-delete-only policy that
     * applies to organization/store/terminal/user/role/configuration rows themselves.
     */
    private void assignPermissions(Role role, List<String> permissionCodes) {
        if (permissionCodes == null) {
            return;
        }
        List<RolePermission> existing = rolePermissionRepository.findAllByRoleIdIn(List.of(role.getId()));
        if (!existing.isEmpty()) {
            rolePermissionRepository.deleteAll(existing);
        }
        List<Permission> permissions = permissionCodes.isEmpty()
                ? Collections.emptyList()
                : permissionRepository.findAllByCodeIn(permissionCodes);
        for (Permission permission : permissions) {
            RolePermission rolePermission = new RolePermission();
            rolePermission.setRole(role);
            rolePermission.setPermission(permission);
            rolePermissionRepository.save(rolePermission);
        }
    }

    private RoleResponse toResponseWithPermissions(Role role) {
        List<RolePermission> rolePermissions = rolePermissionRepository.findAllByRoleIdIn(List.of(role.getId()));
        List<Permission> permissions = rolePermissions.stream().map(RolePermission::getPermission).toList();
        RoleResponse base = roleMapper.toResponse(role);
        return new RoleResponse(base.id(), base.code(), base.name(), base.description(), base.systemRole(),
                roleMapper.toPermissionResponses(permissions));
    }

    private Role getEntity(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Role", id));
    }
}
