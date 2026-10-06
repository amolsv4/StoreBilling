package com.retailpos.sale.repository;

import com.retailpos.sale.entity.Sale;
import com.retailpos.sale.entity.SaleStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Reusable JPA {@link Specification} builder for {@link Sale} list filtering. Mirrors
 * {@code com.retailpos.configuration.repository.AppConfigurationSpecifications}'s style.
 */
public final class SaleSpecifications {

    private SaleSpecifications() {
    }

    public static Specification<Sale> filter(SaleStatus status, Long storeId, Long terminalId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (storeId != null) {
                predicates.add(cb.equal(root.get("store").get("id"), storeId));
            }
            if (terminalId != null) {
                predicates.add(cb.equal(root.get("terminal").get("id"), terminalId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
