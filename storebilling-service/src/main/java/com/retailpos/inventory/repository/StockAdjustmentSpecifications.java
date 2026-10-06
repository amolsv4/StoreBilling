package com.retailpos.inventory.repository;

import com.retailpos.inventory.entity.StockAdjustment;
import com.retailpos.inventory.entity.StockAdjustmentType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Reusable JPA {@link Specification} builder for {@link StockAdjustment} list filtering. */
public final class StockAdjustmentSpecifications {

    private StockAdjustmentSpecifications() {
    }

    /**
     * @param restrictToStoreIds {@code null} = unrestricted; otherwise restricts to this store id
     *                            set (possibly empty). See
     *                            {@link com.retailpos.inventory.util.StoreScopeResolver}.
     */
    public static Specification<StockAdjustment> filter(Long productId,
                                                          StockAdjustmentType adjustmentType,
                                                          Set<Long> restrictToStoreIds) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (productId != null) {
                predicates.add(cb.equal(root.get("productId"), productId));
            }
            if (adjustmentType != null) {
                predicates.add(cb.equal(root.get("adjustmentType"), adjustmentType));
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
