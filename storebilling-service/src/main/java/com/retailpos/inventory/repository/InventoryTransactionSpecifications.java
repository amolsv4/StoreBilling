package com.retailpos.inventory.repository;

import com.retailpos.inventory.entity.InventoryTransaction;
import com.retailpos.inventory.service.InventoryTransactionType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Reusable JPA {@link Specification} builder for {@link InventoryTransaction} list filtering. */
public final class InventoryTransactionSpecifications {

    private InventoryTransactionSpecifications() {
    }

    /**
     * @param restrictToStoreIds {@code null} = unrestricted; otherwise restricts to this store id
     *                            set (possibly empty). See
     *                            {@link com.retailpos.inventory.util.StoreScopeResolver}.
     */
    public static Specification<InventoryTransaction> filter(Long productId,
                                                               InventoryTransactionType type,
                                                               Set<Long> restrictToStoreIds) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (productId != null) {
                predicates.add(cb.equal(root.get("productId"), productId));
            }
            if (type != null) {
                predicates.add(cb.equal(root.get("transactionType"), type));
            }
            if (restrictToStoreIds != null) {
                predicates.add(restrictToStoreIds.isEmpty()
                        ? cb.disjunction()
                        : root.get("storeId").in(restrictToStoreIds));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
