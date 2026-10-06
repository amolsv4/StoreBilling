package com.retailpos.configuration.entity;

/**
 * Configuration scope, in resolution priority order (highest/most-specific first):
 * TERMINAL -&gt; STORE -&gt; ORGANIZATION -&gt; BUSINESS_TYPE -&gt; GLOBAL.
 */
public enum ScopeType {
    GLOBAL,
    BUSINESS_TYPE,
    ORGANIZATION,
    STORE,
    TERMINAL
}
