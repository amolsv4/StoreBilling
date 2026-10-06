package com.retailpos.configuration.service;

import com.retailpos.common.config.CacheConfig;
import com.retailpos.configuration.dto.ConfigurationResponse;
import com.retailpos.configuration.dto.UpdateConfigurationRequest;
import com.retailpos.configuration.entity.AppConfiguration;
import com.retailpos.configuration.entity.DataType;
import com.retailpos.configuration.entity.ScopeType;
import com.retailpos.configuration.mapper.ConfigurationMapper;
import com.retailpos.configuration.repository.AppConfigurationRepository;
import com.retailpos.organization.repository.BusinessTypeRepository;
import com.retailpos.organization.repository.OrganizationRepository;
import com.retailpos.store.repository.StoreRepository;
import com.retailpos.terminal.repository.TerminalRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Exercises the full TERMINAL -&gt; STORE -&gt; ORGANIZATION -&gt; BUSINESS_TYPE -&gt; GLOBAL -&gt;
 * DEFAULT priority chain, plus the {@code @Cacheable}/{@code @CacheEvict} caching behavior.
 *
 * <p>Uses a narrow, hand-built Spring context (only {@link ConfigurationServiceImpl} +
 * {@link CacheConfig}, via {@code @ContextConfiguration} rather than {@code @SpringBootTest}) so
 * the real caching AOP proxy is exercised without pulling in full Spring Boot autoconfiguration
 * (which would otherwise try to build a JPA DataSource this test has no need for) - keeping this
 * a fast, DB-free unit test.
 */
@SpringJUnitConfig
@ContextConfiguration(classes = {ConfigurationServiceImpl.class, CacheConfig.class})
class ConfigurationServiceTest {

    @Autowired
    private ConfigurationService configurationService;

    @MockitoBean
    private AppConfigurationRepository appConfigurationRepository;
    @MockitoBean
    private TerminalRepository terminalRepository;
    @MockitoBean
    private StoreRepository storeRepository;
    @MockitoBean
    private OrganizationRepository organizationRepository;
    @MockitoBean
    private BusinessTypeRepository businessTypeRepository;
    @MockitoBean
    private ConfigurationMapper configurationMapper;

    @Test
    void resolvesTerminalScopeWhenPresent() {
        ConfigContext context = new ConfigContext(10L, 20L, 30L, 40L);
        when(appConfigurationRepository.findByConfigKeyAndScopeTypeAndScopeIdAndActiveTrue(
                "SHOW_GOLD_RATE", ScopeType.TERMINAL, 10L))
                .thenReturn(Optional.of(configOf("SHOW_GOLD_RATE", "false", DataType.BOOLEAN)));

        ResolvedConfig resolved = configurationService.resolve("SHOW_GOLD_RATE", context);

        assertThat(resolved.found()).isTrue();
        assertThat(resolved.value()).isEqualTo("false");
        assertThat(resolved.resolvedScopeType()).isEqualTo("TERMINAL");
        assertThat(resolved.resolvedScopeId()).isEqualTo(10L);
    }

    @Test
    void fallsThroughToStoreWhenNoTerminalMatch() {
        ConfigContext context = new ConfigContext(10L, 20L, 30L, 40L);
        when(appConfigurationRepository.findByConfigKeyAndScopeTypeAndScopeIdAndActiveTrue(
                "DEFAULT_CURRENCY", ScopeType.TERMINAL, 10L)).thenReturn(Optional.empty());
        when(appConfigurationRepository.findByConfigKeyAndScopeTypeAndScopeIdAndActiveTrue(
                "DEFAULT_CURRENCY", ScopeType.STORE, 20L))
                .thenReturn(Optional.of(configOf("DEFAULT_CURRENCY", "INR", DataType.STRING)));

        ResolvedConfig resolved = configurationService.resolve("DEFAULT_CURRENCY", context);

        assertThat(resolved.value()).isEqualTo("INR");
        assertThat(resolved.resolvedScopeType()).isEqualTo("STORE");
    }

    @Test
    void fallsThroughToOrganizationThenBusinessTypeThenGlobal() {
        ConfigContext context = new ConfigContext(null, null, 30L, 40L);
        when(appConfigurationRepository.findByConfigKeyAndScopeTypeAndScopeIdAndActiveTrue(
                "SOME_KEY", ScopeType.ORGANIZATION, 30L)).thenReturn(Optional.empty());
        when(appConfigurationRepository.findByConfigKeyAndScopeTypeAndScopeIdAndActiveTrue(
                "SOME_KEY", ScopeType.BUSINESS_TYPE, 40L))
                .thenReturn(Optional.of(configOf("SOME_KEY", "business-type-value", DataType.STRING)));

        ResolvedConfig resolved = configurationService.resolve("SOME_KEY", context);

        assertThat(resolved.value()).isEqualTo("business-type-value");
        assertThat(resolved.resolvedScopeType()).isEqualTo("BUSINESS_TYPE");
    }

    @Test
    void fallsBackToCompiledInDefaultWhenNoDatabaseRowMatches() {
        ConfigContext context = ConfigContext.empty();
        when(appConfigurationRepository.findByConfigKeyAndScopeTypeAndScopeIdAndActiveTrue(
                eq("DEFAULT_CURRENCY"), eq(ScopeType.GLOBAL), eq(AppConfiguration.GLOBAL_SCOPE_ID)))
                .thenReturn(Optional.empty());

        ResolvedConfig resolved = configurationService.resolve("DEFAULT_CURRENCY", context);

        assertThat(resolved.found()).isTrue();
        assertThat(resolved.resolvedScopeType()).isEqualTo("DEFAULT");
        assertThat(resolved.value()).isEqualTo("USD");
    }

    @Test
    void returnsNotFoundWhenNoMatchAndNoCompiledInDefault() {
        ConfigContext context = ConfigContext.empty();

        ResolvedConfig resolved = configurationService.resolve("SOME_UNDEFINED_KEY_XYZ", context);

        assertThat(resolved.found()).isFalse();
        assertThat(resolved.value()).isNull();
    }

    @Test
    void resolveIsCachedThenEvictedOnUpdate() {
        ConfigContext context = ConfigContext.empty();
        when(appConfigurationRepository.findByConfigKeyAndScopeTypeAndScopeIdAndActiveTrue(
                "DEFAULT_CURRENCY", ScopeType.GLOBAL, AppConfiguration.GLOBAL_SCOPE_ID))
                .thenReturn(Optional.empty());

        configurationService.resolve("DEFAULT_CURRENCY", context);
        configurationService.resolve("DEFAULT_CURRENCY", context);

        verify(appConfigurationRepository, times(1)).findByConfigKeyAndScopeTypeAndScopeIdAndActiveTrue(
                "DEFAULT_CURRENCY", ScopeType.GLOBAL, AppConfiguration.GLOBAL_SCOPE_ID);

        AppConfiguration existing = configOf("DEFAULT_CURRENCY", "USD", DataType.STRING);
        existing.setId(99L);
        when(appConfigurationRepository.findById(99L)).thenReturn(Optional.of(existing));
        when(appConfigurationRepository.save(any())).thenReturn(existing);
        when(configurationMapper.toResponse(any())).thenReturn(
                new ConfigurationResponse(99L, "DEFAULT_CURRENCY", "EUR", DataType.STRING,
                        ScopeType.GLOBAL, AppConfiguration.GLOBAL_SCOPE_ID, "desc", true, null, null));

        configurationService.update(99L, new UpdateConfigurationRequest("EUR", "desc", true));

        configurationService.resolve("DEFAULT_CURRENCY", context);

        verify(appConfigurationRepository, times(2)).findByConfigKeyAndScopeTypeAndScopeIdAndActiveTrue(
                "DEFAULT_CURRENCY", ScopeType.GLOBAL, AppConfiguration.GLOBAL_SCOPE_ID);
    }

    private AppConfiguration configOf(String key, String value, DataType dataType) {
        AppConfiguration configuration = new AppConfiguration();
        configuration.setConfigKey(key);
        configuration.setConfigValue(value);
        configuration.setDataType(dataType);
        configuration.setActive(true);
        return configuration;
    }
}
