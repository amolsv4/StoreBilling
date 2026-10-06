package com.retailpos.inventory.util;

import com.retailpos.auth.AccessValidator;
import com.retailpos.auth.userdetails.UserPrincipal;

import java.util.Set;

/**
 * IDOR guard shared by every list endpoint across the inventory ledger slice (inventory
 * balances/transactions, stock adjustments, purchase receipts, stock transfers, stock takes):
 * every list endpoint's {@code storeId} filter is optional, and a caller without all-store access
 * who omits it must be defaulted to their own accessible store set server-side - never silently
 * fall through to an unfiltered, all-stores query.
 *
 * <p>This does NOT duplicate {@link AccessValidator}'s access-decision logic - an explicit
 * {@code storeId} is still authorized via {@link AccessValidator#assertStoreAccess}. This class
 * only supplies the *default* scope when the caller omits {@code storeId}, using the same
 * {@code allStoreAccess}/{@code storeIds} signal already carried on {@link UserPrincipal}.
 */
public final class StoreScopeResolver {

    private StoreScopeResolver() {
    }

    /**
     * @param requestedStoreId the optional {@code storeId} query parameter, as supplied by the
     *                          caller
     * @return {@code null} if the query should be unrestricted (an explicit {@code storeId} was
     *         already authorized, or the caller has all-store access); otherwise the store id set
     *         the result must be restricted to (possibly empty, meaning "no accessible stores").
     */
    public static Set<Long> resolve(UserPrincipal principal, Long requestedStoreId, AccessValidator accessValidator) {
        if (requestedStoreId != null) {
            accessValidator.assertStoreAccess(principal, requestedStoreId);
            return Set.of(requestedStoreId);
        }
        if (principal == null || principal.isAllStoreAccess()) {
            return null;
        }
        return principal.getStoreIds();
    }
}
