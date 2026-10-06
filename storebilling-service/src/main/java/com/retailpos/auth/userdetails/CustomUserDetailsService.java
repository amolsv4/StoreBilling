package com.retailpos.auth.userdetails;

import com.retailpos.role.entity.RolePermission;
import com.retailpos.role.entity.UserRole;
import com.retailpos.role.repository.RolePermissionRepository;
import com.retailpos.role.repository.UserRoleRepository;
import com.retailpos.user.entity.User;
import com.retailpos.user.repository.UserRepository;
import com.retailpos.user.repository.UserStoreAccessRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Loads a fresh {@link UserPrincipal} - including roles, role-derived permissions, and store
 * access - directly from the database on every call. This is invoked both at login time
 * (AuthServiceImpl) and on every subsequent authenticated request (JwtAuthenticationFilter), so
 * authorization decisions are never based on stale, token-embedded permission data.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserStoreAccessRepository userStoreAccessRepository;

    public CustomUserDetailsService(UserRepository userRepository,
                                     UserRoleRepository userRoleRepository,
                                     RolePermissionRepository rolePermissionRepository,
                                     UserStoreAccessRepository userStoreAccessRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.rolePermissionRepository = rolePermissionRepository;
        this.userStoreAccessRepository = userStoreAccessRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with username: " + username));
        return toPrincipal(user);
    }

    private UserPrincipal toPrincipal(User user) {
        List<UserRole> userRoles = userRoleRepository.findAllByUserId(user.getId());
        List<Long> roleIds = userRoles.stream().map(ur -> ur.getRole().getId()).toList();
        List<String> roleCodes = userRoles.stream().map(ur -> ur.getRole().getCode()).toList();

        Set<GrantedAuthority> authorities = new HashSet<>();
        for (UserRole userRole : userRoles) {
            authorities.add(UserPrincipal.roleAuthority(userRole.getRole().getCode()));
        }
        if (!roleIds.isEmpty()) {
            List<RolePermission> rolePermissions = rolePermissionRepository.findAllByRoleIdIn(roleIds);
            for (RolePermission rolePermission : rolePermissions) {
                authorities.add(UserPrincipal.permissionAuthority(rolePermission.getPermission().getCode()));
            }
        }

        Set<Long> storeIds = user.isAllStoreAccess()
                ? Set.of()
                : userStoreAccessRepository.findAllByUserId(user.getId()).stream()
                        .map(access -> access.getStore().getId())
                        .collect(Collectors.toSet());

        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                user.isActive(),
                user.isAccountLocked(),
                user.getOrganization() != null ? user.getOrganization().getId() : null,
                user.isAllStoreAccess(),
                storeIds,
                authorities,
                roleCodes);
    }
}
