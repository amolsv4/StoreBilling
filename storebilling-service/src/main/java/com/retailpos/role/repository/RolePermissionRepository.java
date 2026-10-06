package com.retailpos.role.repository;

import com.retailpos.role.entity.RolePermission;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    @EntityGraph(attributePaths = {"permission"})
    List<RolePermission> findAllByRoleIdIn(Collection<Long> roleIds);
}
