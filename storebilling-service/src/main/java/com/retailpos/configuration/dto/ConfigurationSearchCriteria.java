package com.retailpos.configuration.dto;

import com.retailpos.configuration.entity.ScopeType;

/** Optional admin-list filters for {@code GET /api/v1/config}. Any field may be {@code null}. */
public record ConfigurationSearchCriteria(ScopeType scopeType, Long scopeId, String key) {
}
