package com.retailpos.stocktake.repository;

import com.retailpos.stocktake.entity.StockTake;
import com.retailpos.stocktake.entity.StockTakeStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class StockTakeSpecifications {

    private StockTakeSpecifications() {
    }

    /**
     * @param restrictToStoreIds {@code null} = unrestricted; otherwise restricts to this store id
     *                            set (possibly empty). See
     *                            {@link com.retailpos.inventory.util.StoreScopeResolver}.
     */
    public static Specification<StockTake> filter(Long organizationId, StockTakeStatus status,
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
