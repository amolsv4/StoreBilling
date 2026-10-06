package com.retailpos.inventory.repository;

import com.retailpos.inventory.entity.Inventory;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Reusable JPA {@link Specification} builder for {@link Inventory} list filtering. */
public final class InventorySpecifications {

    private InventorySpecifications() {
    }

    /**
     * @param restrictToStoreIds {@code null} = unrestricted; otherwise restricts to this store id
     *                            set (possibly empty, meaning "no accessible stores" - the IDOR
     *                            default-scope case). See
     *                            {@link com.retailpos.inventory.util.StoreScopeResolver}.
     */
    public static Specification<Inventory> filter(Long productId, Set<Long> restrictToStoreIds) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (productId != null) {
                predicates.add(cb.equal(root.get("productId"), productId));
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
