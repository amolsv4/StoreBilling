package com.retailpos.inventory.util;

import com.retailpos.auth.AccessValidator;
import com.retailpos.auth.userdetails.UserPrincipal;
import com.retailpos.terminal.repository.TerminalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Verifies the IDOR guard described in plan §3: a list endpoint's optional {@code storeId} filter
 * must default to the caller's own accessible store set when omitted, for a caller without
 * all-store access - never silently fall through to an unfiltered, all-stores query.
 */
@ExtendWith(MockitoExtension.class)
class StoreScopeResolverTest {

    @Mock
    private TerminalRepository terminalRepository;

    private AccessValidator accessValidator;

    @BeforeEach
    void setUp() {
        accessValidator = new AccessValidator(terminalRepository);
    }

    private UserPrincipal principal(boolean allStoreAccess, Set<Long> storeIds, List<GrantedAuthority> authorities) {
        return new UserPrincipal(1L, "user1", "hash", true, false, 100L, allStoreAccess, storeIds, authorities,
                List.of());
    }

    @Test
    void omittedStoreIdDefaultsToTheCallersOwnAccessibleStoreSetWhenNotAllStoreAccess() {
        UserPrincipal restrictedUser = principal(false, Set.of(5L, 7L), List.of());

        Set<Long> resolved = StoreScopeResolver.resolve(restrictedUser, null, accessValidator);

        assertThat(resolved).containsExactlyInAnyOrder(5L, 7L);
    }

    @Test
    void omittedStoreIdDefaultsToAnEmptySetWhenTheCallerHasNoStoreAccessAtAll() {
        UserPrincipal noAccessUser = principal(false, Set.of(), List.of());

        Set<Long> resolved = StoreScopeResolver.resolve(noAccessUser, null, accessValidator);

        // An empty restriction set (not null/unrestricted) - callers must translate this into a
        // query that returns no rows, never an unfiltered query. This is the crux of the IDOR guard.
        assertThat(resolved).isNotNull().isEmpty();
    }

    @Test
    void omittedStoreIdIsUnrestrictedForAnAllStoreAccessCaller() {
        UserPrincipal allStoreUser = principal(true, Set.of(), List.of());

        Set<Long> resolved = StoreScopeResolver.resolve(allStoreUser, null, accessValidator);

        assertThat(resolved).isNull();
    }

    @Test
    void explicitStoreIdWithinTheCallersAccessResolvesToASingletonSetAndDoesNotThrow() {
        UserPrincipal restrictedUser = principal(false, Set.of(5L, 7L), List.of());

        Set<Long> resolved = StoreScopeResolver.resolve(restrictedUser, 5L, accessValidator);

        assertThat(resolved).containsExactly(5L);
    }

    @Test
    void explicitStoreIdOutsideTheCallersAccessIsRejected() {
        UserPrincipal restrictedUser = principal(false, Set.of(5L, 7L), List.of());

        assertThatExceptionOfType(AccessDeniedException.class)
                .isThrownBy(() -> StoreScopeResolver.resolve(restrictedUser, 999L, accessValidator));
    }
}
