package com.retailpos.role.repository;

import com.retailpos.role.entity.UserRole;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    @EntityGraph(attributePaths = {"role"})
    List<UserRole> findAllByUserId(Long userId);

    boolean existsByUserIdAndRoleId(Long userId, Long roleId);

    @Query("select case when count(ur) > 0 then true else false end "
            + "from UserRole ur where ur.role.code = :roleCode")
    boolean existsByRoleCode(@Param("roleCode") String roleCode);
}
