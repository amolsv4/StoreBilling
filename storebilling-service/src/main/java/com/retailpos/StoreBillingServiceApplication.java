package com.retailpos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * StoreBillingService Phase 1 entry point.
 *
 * <p>Modular-monolith foundation: project setup, DB schema (Flyway), JWT security, the
 * Organization -> Store -> Terminal hierarchy, and the multi-scope configuration engine.
 * Product catalog, pricing, tax, inventory, POS sales, payments, returns, shift, reporting,
 * MPOS, offline sync, and Gold/Grocery-specific features are explicitly out of scope for this
 * phase.
 *
 * <p>{@code @EnableCaching} lives on {@link com.retailpos.common.config.CacheConfig} and
 * {@code @EnableJpaAuditing} lives on {@link com.retailpos.common.config.JpaAuditingConfig},
 * keeping cross-cutting enablement co-located with the beans they configure.
 */
@SpringBootApplication
public class StoreBillingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StoreBillingServiceApplication.class, args);
    }
}
