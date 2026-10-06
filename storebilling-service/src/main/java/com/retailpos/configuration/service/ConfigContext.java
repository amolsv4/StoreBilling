package com.retailpos.configuration.service;

/**
 * The resolution context for a configuration lookup. Any subset of fields may be supplied - if
 * only {@code terminalId} is given, {@code ConfigurationServiceImpl} derives the rest by walking
 * Terminal -&gt; Store -&gt; Organization/BusinessType via repositories.
 */
public record ConfigContext(Long terminalId, Long storeId, Long organizationId, Long businessTypeId) {

    public static ConfigContext empty() {
        return new ConfigContext(null, null, null, null);
    }

    public static ConfigContext ofTerminal(Long terminalId) {
        return new ConfigContext(terminalId, null, null, null);
    }

    public static ConfigContext ofStore(Long storeId) {
        return new ConfigContext(null, storeId, null, null);
    }
}
