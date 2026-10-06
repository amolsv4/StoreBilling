package com.retailpos.configuration.controller;

import com.retailpos.auth.AccessValidator;
import com.retailpos.auth.userdetails.UserPrincipal;
import com.retailpos.common.dto.ApiResponse;
import com.retailpos.common.exception.ResourceNotFoundException;
import com.retailpos.configuration.dto.ConfigurationResponse;
import com.retailpos.configuration.dto.ConfigurationSearchCriteria;
import com.retailpos.configuration.dto.CreateConfigurationRequest;
import com.retailpos.configuration.dto.UpdateConfigurationRequest;
import com.retailpos.configuration.entity.ScopeType;
import com.retailpos.configuration.service.ConfigContext;
import com.retailpos.configuration.service.ConfigurationService;
import com.retailpos.configuration.service.ResolvedConfig;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * No DELETE endpoint - configuration rows are only ever soft-toggled via {@code PUT active=false}.
 */
@RestController
@RequestMapping("/api/v1/config")
public class ConfigurationController {

    private final ConfigurationService configurationService;
    private final AccessValidator accessValidator;

    public ConfigurationController(ConfigurationService configurationService, AccessValidator accessValidator) {
        this.configurationService = configurationService;
        this.accessValidator = accessValidator;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CONFIGURATION_VIEW')")
    public ApiResponse<Page<ConfigurationResponse>> list(@RequestParam(required = false) ScopeType scopeType,
                                                           @RequestParam(required = false) Long scopeId,
                                                           @RequestParam(required = false) String key,
                                                           Pageable pageable) {
        ConfigurationSearchCriteria criteria = new ConfigurationSearchCriteria(scopeType, scopeId, key);
        return ApiResponse.success(configurationService.search(criteria, pageable));
    }

    /**
     * Resolves {@code key} through the TERMINAL -&gt; STORE -&gt; ORGANIZATION -&gt; BUSINESS_TYPE
     * -&gt; GLOBAL -&gt; DEFAULT chain. The response includes which scope it actually resolved
     * from, for debuggability. If a storeId/terminalId is supplied, it is checked against the
     * caller's own store access (IDOR prevention) before resolution proceeds.
     */
    @GetMapping("/{key}")
    @PreAuthorize("hasAuthority('CONFIGURATION_VIEW')")
    public ApiResponse<ResolvedConfig> resolve(@PathVariable String key,
                                                @RequestParam(required = false) Long terminalId,
                                                @RequestParam(required = false) Long storeId,
                                                @RequestParam(required = false) Long organizationId,
                                                @RequestParam(required = false) Long businessTypeId,
                                                @AuthenticationPrincipal UserPrincipal principal) {
        accessValidator.assertStoreAccess(principal, storeId);
        accessValidator.assertTerminalAccess(principal, terminalId);
        ConfigContext context = new ConfigContext(terminalId, storeId, organizationId, businessTypeId);
        ResolvedConfig resolved = configurationService.resolve(key, context);
        if (!resolved.found()) {
            throw ResourceNotFoundException.forEntity("Configuration key", key);
        }
        return ApiResponse.success(resolved);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('CONFIGURATION_CREATE')")
    public ApiResponse<ConfigurationResponse> create(@Valid @RequestBody CreateConfigurationRequest request) {
        return ApiResponse.success("Configuration created.", configurationService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CONFIGURATION_UPDATE')")
    public ApiResponse<ConfigurationResponse> update(@PathVariable Long id,
                                                       @Valid @RequestBody UpdateConfigurationRequest request) {
        return ApiResponse.success("Configuration updated.", configurationService.update(id, request));
    }
}
