package com.retailpos.configuration.service;

import com.retailpos.common.config.CacheConfig;
import com.retailpos.common.exception.DuplicateResourceException;
import com.retailpos.common.exception.ResourceNotFoundException;
import com.retailpos.configuration.dto.ConfigurationResponse;
import com.retailpos.configuration.dto.ConfigurationSearchCriteria;
import com.retailpos.configuration.dto.CreateConfigurationRequest;
import com.retailpos.configuration.dto.UpdateConfigurationRequest;
import com.retailpos.configuration.entity.AppConfiguration;
import com.retailpos.configuration.entity.DataType;
import com.retailpos.configuration.entity.ScopeType;
import com.retailpos.configuration.mapper.ConfigurationMapper;
import com.retailpos.configuration.repository.AppConfigurationRepository;
import com.retailpos.configuration.repository.AppConfigurationSpecifications;
import com.retailpos.organization.repository.BusinessTypeRepository;
import com.retailpos.organization.repository.OrganizationRepository;
import com.retailpos.store.entity.Store;
import com.retailpos.store.repository.StoreRepository;
import com.retailpos.terminal.entity.Terminal;
import com.retailpos.terminal.repository.TerminalRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The configuration resolution engine. See {@link ConfigurationService} for the priority order.
 *
 * <p>Algorithm for {@link #resolve}: enrich the given {@link ConfigContext} by walking
 * Terminal -&gt; Store -&gt; Organization/BusinessType for any ids not already supplied, build the
 * ordered (scopeType, scopeId) list, and return the first active row found at each scope in turn;
 * fall back to {@link ConfigKeyDefaults} if nothing matches in the database; otherwise "not found".
 */
@Service
public class ConfigurationServiceImpl implements ConfigurationService {

    private final AppConfigurationRepository appConfigurationRepository;
    private final TerminalRepository terminalRepository;
    private final StoreRepository storeRepository;
    private final OrganizationRepository organizationRepository;
    private final BusinessTypeRepository businessTypeRepository;
    private final ConfigurationMapper configurationMapper;

    public ConfigurationServiceImpl(AppConfigurationRepository appConfigurationRepository,
                                     TerminalRepository terminalRepository,
                                     StoreRepository storeRepository,
                                     OrganizationRepository organizationRepository,
                                     BusinessTypeRepository businessTypeRepository,
                                     ConfigurationMapper configurationMapper) {
        this.appConfigurationRepository = appConfigurationRepository;
        this.terminalRepository = terminalRepository;
        this.storeRepository = storeRepository;
        this.organizationRepository = organizationRepository;
        this.businessTypeRepository = businessTypeRepository;
        this.configurationMapper = configurationMapper;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.CONFIG_RESOLUTION_CACHE)
    public ResolvedConfig resolve(String configKey, ConfigContext context) {
        ConfigContext enriched = enrichContext(context);
        for (ScopePair pair : buildScopePairsInPriorityOrder(enriched)) {
            Optional<AppConfiguration> match = appConfigurationRepository
                    .findByConfigKeyAndScopeTypeAndScopeIdAndActiveTrue(configKey, pair.scopeType(), pair.scopeId());
            if (match.isPresent()) {
                AppConfiguration configuration = match.get();
                return new ResolvedConfig(configKey, configuration.getConfigValue(), configuration.getDataType(),
                        pair.scopeType().name(), pair.scopeId(), true);
            }
        }
        if (ConfigKeyDefaults.hasDefault(configKey)) {
            return new ResolvedConfig(configKey, ConfigKeyDefaults.get(configKey), DataType.STRING, "DEFAULT", null, true);
        }
        return ResolvedConfig.notFound(configKey);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, ResolvedConfig> resolveAll(ConfigContext context) {
        ConfigContext enriched = enrichContext(context);
        List<ScopePair> scopePairs = buildScopePairsInPriorityOrder(enriched);

        Set<String> keys = new LinkedHashSet<>();
        appConfigurationRepository.findAll(AppConfigurationSpecifications.matchesAnyScope(scopePairs))
                .forEach(configuration -> keys.add(configuration.getConfigKey()));
        keys.addAll(ConfigKeyDefaults.keys());

        Map<String, ResolvedConfig> resolved = new LinkedHashMap<>();
        for (String key : keys) {
            // Calling resolve(...) here is a plain internal method call (not through the Spring
            // proxy), so it deliberately does not go through the @Cacheable interception for
            // this batch path - resolveAll always computes fresh. Individual resolve() calls
            // made by other callers through the proxy are still cached/evicted normally.
            resolved.put(key, resolve(key, enriched));
        }
        return resolved;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheConfig.CONFIG_RESOLUTION_CACHE, allEntries = true)
    public ConfigurationResponse create(CreateConfigurationRequest request) {
        validateScopeIdReference(request.scopeType(), request.scopeId());
        if (appConfigurationRepository.existsByConfigKeyAndScopeTypeAndScopeId(
                request.configKey(), request.scopeType(), request.scopeId())) {
            throw new DuplicateResourceException("Configuration key '" + request.configKey() + "' already has a "
                    + "value at scope " + request.scopeType() + "/" + request.scopeId() + ".");
        }
        AppConfiguration entity = configurationMapper.toEntity(request);
        AppConfiguration saved = appConfigurationRepository.save(entity);
        return configurationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheConfig.CONFIG_RESOLUTION_CACHE, allEntries = true)
    public ConfigurationResponse update(Long id, UpdateConfigurationRequest request) {
        AppConfiguration entity = appConfigurationRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("AppConfiguration", id));
        configurationMapper.updateEntityFromRequest(request, entity);
        entity.setActive(request.active());
        AppConfiguration saved = appConfigurationRepository.save(entity);
        return configurationMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConfigurationResponse> search(ConfigurationSearchCriteria criteria, Pageable pageable) {
        return appConfigurationRepository.findAll(AppConfigurationSpecifications.search(criteria), pageable)
                .map(configurationMapper::toResponse);
    }

    /** Validates scope_id against the correct table for scope_type (no DB-level FK exists for this polymorphic column). */
    private void validateScopeIdReference(ScopeType scopeType, Long scopeId) {
        boolean valid = switch (scopeType) {
            case GLOBAL -> scopeId != null && scopeId == AppConfiguration.GLOBAL_SCOPE_ID;
            case BUSINESS_TYPE -> businessTypeRepository.existsById(scopeId);
            case ORGANIZATION -> organizationRepository.existsById(scopeId);
            case STORE -> storeRepository.existsById(scopeId);
            case TERMINAL -> terminalRepository.existsById(scopeId);
        };
        if (!valid) {
            throw ResourceNotFoundException.forEntity(scopeType.name(), scopeId);
        }
    }

    /** Fills in any missing context ids by walking Terminal -&gt; Store -&gt; Organization/BusinessType. */
    private ConfigContext enrichContext(ConfigContext context) {
        Long terminalId = context.terminalId();
        Long storeId = context.storeId();
        Long organizationId = context.organizationId();
        Long businessTypeId = context.businessTypeId();

        if (terminalId != null && storeId == null) {
            Terminal terminal = terminalRepository.findById(terminalId).orElse(null);
            if (terminal != null) {
                storeId = terminal.getStore().getId();
            }
        }
        if (storeId != null && (organizationId == null || businessTypeId == null)) {
            Store store = storeRepository.findById(storeId).orElse(null);
            if (store != null) {
                if (organizationId == null) {
                    organizationId = store.getOrganization().getId();
                }
                if (businessTypeId == null) {
                    businessTypeId = store.getBusinessType().getId();
                }
            }
        }
        return new ConfigContext(terminalId, storeId, organizationId, businessTypeId);
    }

    /** TERMINAL -&gt; STORE -&gt; ORGANIZATION -&gt; BUSINESS_TYPE -&gt; GLOBAL, skipping any null ids. */
    private List<ScopePair> buildScopePairsInPriorityOrder(ConfigContext context) {
        List<ScopePair> pairs = new ArrayList<>();
        if (context.terminalId() != null) {
            pairs.add(new ScopePair(ScopeType.TERMINAL, context.terminalId()));
        }
        if (context.storeId() != null) {
            pairs.add(new ScopePair(ScopeType.STORE, context.storeId()));
        }
        if (context.organizationId() != null) {
            pairs.add(new ScopePair(ScopeType.ORGANIZATION, context.organizationId()));
        }
        if (context.businessTypeId() != null) {
            pairs.add(new ScopePair(ScopeType.BUSINESS_TYPE, context.businessTypeId()));
        }
        pairs.add(new ScopePair(ScopeType.GLOBAL, AppConfiguration.GLOBAL_SCOPE_ID));
        return pairs;
    }
}
