package com.retailpos.configuration.service;

import com.retailpos.configuration.entity.ScopeType;

/**
 * A single (scopeType, scopeId) pair, used to build the priority-ordered list of scopes to check
 * during resolution, and to build the "matches any of these scopes" query predicate.
 */
public record ScopePair(ScopeType scopeType, Long scopeId) {
}
