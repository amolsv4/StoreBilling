package com.retailpos.stocktransfer.repository;

import com.retailpos.stocktransfer.entity.StockTransfer;
import com.retailpos.stocktransfer.entity.StockTransferStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class StockTransferSpecifications {

    private StockTransferSpecifications() {
    }

    /**
     * @param restrictToStoreIds {@code null} = unrestricted; otherwise a transfer matches only if
     *                            its from-store OR to-store is in this set (possibly empty). See
     *                            {@link com.retailpos.inventory.util.StoreScopeResolver}.
     */
    public static Specification<StockTransfer> filter(Long organizationId, StockTransferStatus status,
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
                        : cb.or(root.get("fromStore").get("id").in(restrictToStoreIds),
                                root.get("toStore").get("id").in(restrictToStoreIds)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
