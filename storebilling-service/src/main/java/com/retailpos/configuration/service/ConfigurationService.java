package com.retailpos.configuration.service;

import com.retailpos.configuration.dto.ConfigurationResponse;
import com.retailpos.configuration.dto.ConfigurationSearchCriteria;
import com.retailpos.configuration.dto.CreateConfigurationRequest;
import com.retailpos.configuration.dto.UpdateConfigurationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * The multi-scope configuration engine. Resolution priority (highest/most-specific first):
 * TERMINAL -&gt; STORE -&gt; ORGANIZATION -&gt; BUSINESS_TYPE -&gt; GLOBAL -&gt; DEFAULT (a small
 * compiled-in fallback - see {@link ConfigKeyDefaults} - not a database row).
 */
public interface ConfigurationService {

    ResolvedConfig resolve(String configKey, ConfigContext context);

    Map<String, ResolvedConfig> resolveAll(ConfigContext context);

    ConfigurationResponse create(CreateConfigurationRequest request);

    ConfigurationResponse update(Long id, UpdateConfigurationRequest request);

    Page<ConfigurationResponse> search(ConfigurationSearchCriteria criteria, Pageable pageable);
}
