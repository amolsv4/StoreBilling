package com.retailpos.configuration.service;

import java.util.Map;
import java.util.Set;

/**
 * Small compiled-in fallback values used only when a configuration key has no matching row at
 * any scope (TERMINAL/STORE/ORGANIZATION/BUSINESS_TYPE/GLOBAL) in the database. This is the
 * final "DEFAULT" link in the resolution chain - it is NOT a database table.
 */
public final class ConfigKeyDefaults {

    private static final Map<String, String> DEFAULTS = Map.of(
            "DEFAULT_CURRENCY", "USD",
            "SHOW_GOLD_RATE", "false",
            "SHOW_BATCH", "false",
            "DEFAULT_TIMEZONE", "UTC"
    );

    private ConfigKeyDefaults() {
    }

    public static boolean hasDefault(String configKey) {
        return DEFAULTS.containsKey(configKey);
    }

    public static String get(String configKey) {
        return DEFAULTS.get(configKey);
    }

    public static Set<String> keys() {
        return DEFAULTS.keySet();
    }
}
