package com.retailpos.auth.userdetails;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Spring Security principal for an authenticated app_user. Always built fresh from the database
 * (see {@link CustomUserDetailsService}) - both at login time and on every subsequent request
 * inside {@code JwtAuthenticationFilter} - so role/permission/store-access data can never go
 * stale between issuance and use, even though the JWT itself also carries a point-in-time copy
 * of roles/storeAccess for client-side convenience.
 */
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String username;
    private final String passwordHash;
    private final boolean active;
    private final boolean accountLocked;
    private final Long organizationId;
    private final boolean allStoreAccess;
    private final Set<Long> storeIds;
    private final Collection<GrantedAuthority> authorities;
    private final List<String> roleCodes;

    public UserPrincipal(Long id, String username, String passwordHash, boolean active, boolean accountLocked,
                          Long organizationId, boolean allStoreAccess, Set<Long> storeIds,
                          Collection<GrantedAuthority> authorities, List<String> roleCodes) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.active = active;
        this.accountLocked = accountLocked;
        this.organizationId = organizationId;
        this.allStoreAccess = allStoreAccess;
        this.storeIds = storeIds;
        this.authorities = authorities;
        this.roleCodes = roleCodes;
    }

    public Long getId() {
        return id;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public boolean isAllStoreAccess() {
        return allStoreAccess;
    }

    public Set<Long> getStoreIds() {
        return storeIds;
    }

    /**
     * The user's actual assigned role codes (e.g. {@code SUPER_ADMIN}), populated directly by
     * {@link CustomUserDetailsService} from {@code user_role}/{@code role} - NOT derived by
     * string-matching a {@code ROLE_} prefix on {@link #getAuthorities()}. That naive approach
     * previously broke because the {@code ROLE} permission *module* (managing Role records) has
     * its own legitimate permission codes {@code ROLE_VIEW}/{@code ROLE_CREATE}/{@code ROLE_UPDATE},
     * which collide with the {@code ROLE_} prefix Spring Security's {@code roleAuthority(...)} uses
     * to denote a role identity - a user with both a role and any of those permissions (e.g.
     * SUPER_ADMIN) would incorrectly get "VIEW"/"CREATE"/"UPDATE" fragments mixed into their role
     * list.
     */
    public List<String> getRoleCodes() {
        return roleCodes;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    /** Convenience factory for a single role authority, used by CustomUserDetailsService. */
    public static GrantedAuthority roleAuthority(String roleCode) {
        return new SimpleGrantedAuthority("ROLE_" + roleCode);
    }

    /** Convenience factory for a permission authority, used by CustomUserDetailsService. */
    public static GrantedAuthority permissionAuthority(String permissionCode) {
        return new SimpleGrantedAuthority(permissionCode);
    }
}
