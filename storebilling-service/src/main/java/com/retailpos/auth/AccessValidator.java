package com.retailpos.auth;

import com.retailpos.auth.userdetails.UserPrincipal;
import com.retailpos.terminal.repository.TerminalRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * IDOR-prevention checkpoint: given the authenticated principal's store access (all-stores flag
 * or explicit store id set) and a requested storeId/terminalId, decides whether the request may
 * proceed. Controllers/services that accept a storeId/terminalId parameter should call this
 * before acting on it.
 */
@Component
public class AccessValidator {

    private final TerminalRepository terminalRepository;

    public AccessValidator(TerminalRepository terminalRepository) {
        this.terminalRepository = terminalRepository;
    }

    public boolean hasStoreAccess(UserPrincipal principal, Long storeId) {
        if (storeId == null || principal == null) {
            return true;
        }
        if (principal.isAllStoreAccess() || hasAdminBypass(principal)) {
            return true;
        }
        return principal.getStoreIds().contains(storeId);
    }

    public boolean hasTerminalAccess(UserPrincipal principal, Long terminalId) {
        if (terminalId == null || principal == null) {
            return true;
        }
        if (principal.isAllStoreAccess() || hasAdminBypass(principal)) {
            return true;
        }
        Long storeId = terminalRepository.findById(terminalId)
                .map(terminal -> terminal.getStore().getId())
                .orElse(null);
        return storeId != null && principal.getStoreIds().contains(storeId);
    }

    /** Throws 403 (mapped by GlobalExceptionHandler) if the principal lacks access to the store. */
    public void assertStoreAccess(UserPrincipal principal, Long storeId) {
        if (!hasStoreAccess(principal, storeId)) {
            throw new AccessDeniedException("You do not have access to store id " + storeId);
        }
    }

    /** Throws 403 (mapped by GlobalExceptionHandler) if the principal lacks access to the terminal. */
    public void assertTerminalAccess(UserPrincipal principal, Long terminalId) {
        if (!hasTerminalAccess(principal, terminalId)) {
            throw new AccessDeniedException("You do not have access to terminal id " + terminalId);
        }
    }

    private boolean hasAdminBypass(UserPrincipal principal) {
        for (GrantedAuthority authority : principal.getAuthorities()) {
            String value = authority.getAuthority();
            if ("ROLE_SUPER_ADMIN".equals(value) || "ROLE_ADMIN".equals(value)) {
                return true;
            }
        }
        return false;
    }
}
