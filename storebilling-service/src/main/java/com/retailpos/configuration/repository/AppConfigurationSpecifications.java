package com.retailpos.configuration.repository;

import com.retailpos.configuration.dto.ConfigurationSearchCriteria;
import com.retailpos.configuration.entity.AppConfiguration;
import com.retailpos.configuration.service.ScopePair;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/** Reusable JPA {@link Specification} builders for {@link AppConfiguration}. */
public final class AppConfigurationSpecifications {

    private AppConfigurationSpecifications() {
    }

    /** Builds the admin-list search specification from optional filter criteria. */
    public static Specification<AppConfiguration> search(ConfigurationSearchCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (criteria != null) {
                if (criteria.scopeType() != null) {
                    predicates.add(cb.equal(root.get("scopeType"), criteria.scopeType()));
                }
                if (criteria.scopeId() != null) {
                    predicates.add(cb.equal(root.get("scopeId"), criteria.scopeId()));
                }
                if (StringUtils.hasText(criteria.key())) {
                    predicates.add(cb.like(cb.lower(root.get("configKey")), "%" + criteria.key().toLowerCase() + "%"));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Matches active rows whose (scopeType, scopeId) equals any of the given pairs - used to
     * resolve every configuration key visible to a given {@link com.retailpos.configuration.service.ConfigContext}.
     */
    public static Specification<AppConfiguration> matchesAnyScope(List<ScopePair> scopePairs) {
        return (root, query, cb) -> {
            List<Predicate> scopePredicates = new ArrayList<>();
            for (ScopePair pair : scopePairs) {
                scopePredicates.add(cb.and(
                        cb.equal(root.get("scopeType"), pair.scopeType()),
                        cb.equal(root.get("scopeId"), pair.scopeId())));
            }
            Predicate anyScope = cb.or(scopePredicates.toArray(new Predicate[0]));
            return cb.and(cb.isTrue(root.get("active")), anyScope);
        };
    }
}
