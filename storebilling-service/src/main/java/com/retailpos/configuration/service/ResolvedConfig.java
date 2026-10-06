package com.retailpos.configuration.service;

import com.retailpos.configuration.entity.DataType;

/**
 * The outcome of resolving a single configuration key through the scope chain. {@code
 * resolvedScopeType} is one of the {@link com.retailpos.configuration.entity.ScopeType} names, or
 * {@code "DEFAULT"} when satisfied from the compiled-in {@link ConfigKeyDefaults} fallback, or
 * {@code null} when the key could not be resolved at all.
 */
public record ResolvedConfig(
        String configKey,
        String value,
        DataType dataType,
        String resolvedScopeType,
        Long resolvedScopeId,
        boolean found
) {

    public static ResolvedConfig notFound(String configKey) {
        return new ResolvedConfig(configKey, null, null, null, null, false);
    }
}
