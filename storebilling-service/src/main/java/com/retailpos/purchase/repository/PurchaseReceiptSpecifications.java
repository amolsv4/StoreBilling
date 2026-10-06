package com.retailpos.purchase.repository;

import com.retailpos.purchase.entity.PurchaseReceipt;
import com.retailpos.purchase.entity.PurchaseReceiptStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class PurchaseReceiptSpecifications {

    private PurchaseReceiptSpecifications() {
    }

    /**
     * @param restrictToStoreIds {@code null} = unrestricted; otherwise restricts to this store id
     *                            set (possibly empty). See
     *                            {@link com.retailpos.inventory.util.StoreScopeResolver}.
     */
    public static Specification<PurchaseReceipt> filter(Long organizationId, PurchaseReceiptStatus status,
                                                          Set<Long> restrictToStoreIds) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (organizationId != null) {
                predicates.add(cb.equal(root.get("organization").get("id"), organizationId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (restrictToStoreIds != null) {
                predicates.add(restrictToStoreIds.isEmpty()
                        ? cb.disjunction()
                        : root.get("store").get("id").in(restrictToStoreIds));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
