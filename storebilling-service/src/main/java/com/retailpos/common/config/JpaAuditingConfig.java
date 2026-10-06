package com.retailpos.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Wires Spring Data JPA auditing ({@code @CreatedBy}/{@code @LastModifiedBy} on
 * {@link com.retailpos.common.entity.BaseAuditableEntity}) to the currently authenticated
 * username, falling back to {@code "SYSTEM"} when there is no authenticated user (e.g. Flyway
 * seed data, startup runners, or system-initiated writes).
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

    public static final String SYSTEM_AUDITOR = "SYSTEM";

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()
                    || authentication.getPrincipal() == null
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.of(SYSTEM_AUDITOR);
            }
            String name = authentication.getName();
            return Optional.of(name == null || name.isBlank() ? SYSTEM_AUDITOR : name);
        };
    }
}
