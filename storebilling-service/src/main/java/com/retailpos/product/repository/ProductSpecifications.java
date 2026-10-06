package com.retailpos.product.repository;

import com.retailpos.product.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Reusable JPA {@link Specification} builders for {@link Product}. Mirrors
 * {@code com.retailpos.configuration.repository.AppConfigurationSpecifications}'s style.
 */
public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    /**
     * Builds the product list-endpoint search specification: optional organization/category
     * equality filters plus an optional {@code search} term LIKE-matched (case-insensitive)
     * across {@code name}/{@code sku}/{@code barcode}.
     */
    public static Specification<Product> search(Long organizationId, Long categoryId, String search) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (organizationId != null) {
                predicates.add(cb.equal(root.get("organization").get("id"), organizationId));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            }
            if (StringUtils.hasText(search)) {
                String likePattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), likePattern),
                        cb.like(cb.lower(root.get("sku")), likePattern),
                        cb.like(cb.lower(root.get("barcode")), likePattern)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
