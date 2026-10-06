package com.retailpos.inventory.service;

import com.retailpos.inventory.dto.InventoryResponse;
import com.retailpos.inventory.dto.InventoryTransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

/**
 * Read-only listing for inventory balances/transactions, kept separate from the write-side
 * {@link InventoryService} choke-point interface (which is not to be extended with query
 * methods).
 */
public interface InventoryQueryService {

    /** @param storeIdFilter {@code null} = unrestricted; see {@code StoreScopeResolver}. */
    Page<InventoryResponse> list(Long productId, Set<Long> storeIdFilter, Pageable pageable);

    Page<InventoryTransactionResponse> listTransactions(Long productId, InventoryTransactionType type,
                                                          Set<Long> storeIdFilter, Pageable pageable);
}
