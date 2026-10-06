package com.retailpos.configuration.repository;

import com.retailpos.configuration.entity.AppConfiguration;
import com.retailpos.configuration.entity.ScopeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface AppConfigurationRepository
        extends JpaRepository<AppConfiguration, Long>, JpaSpecificationExecutor<AppConfiguration> {

    Optional<AppConfiguration> findByConfigKeyAndScopeTypeAndScopeIdAndActiveTrue(
            String configKey, ScopeType scopeType, Long scopeId);

    boolean existsByConfigKeyAndScopeTypeAndScopeId(String configKey, ScopeType scopeType, Long scopeId);

    List<AppConfiguration> findAllByActiveTrue();
}
