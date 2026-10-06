package com.retailpos.integration;

import com.retailpos.organization.repository.BusinessTypeRepository;
import com.retailpos.organization.repository.OrganizationRepository;
import com.retailpos.role.repository.PermissionRepository;
import com.retailpos.role.repository.RoleRepository;
import com.retailpos.store.repository.StoreRepository;
import com.retailpos.terminal.repository.TerminalRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Lets Flyway run every migration (V1-V8, since the "local" profile is active) against a real
 * MySQL 8.4 container, then sanity-checks that both the schema and the seed/demo data landed as
 * described in the migration files.
 */
class FlywayMigrationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private BusinessTypeRepository businessTypeRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PermissionRepository permissionRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private TerminalRepository terminalRepository;

    @Test
    void migrationsApplySchemaAndSeedData() {
        assertThat(businessTypeRepository.findAll()).hasSize(3);
        assertThat(businessTypeRepository.findByCode("GOLD")).isPresent();
        assertThat(businessTypeRepository.findByCode("GROCERY")).isPresent();
        assertThat(businessTypeRepository.findByCode("GENERAL")).isPresent();

        assertThat(roleRepository.findAll()).hasSize(7);
        assertThat(roleRepository.findByCode("SUPER_ADMIN")).isPresent();

        assertThat(permissionRepository.findAll()).isNotEmpty();

        assertThat(organizationRepository.findByCode("DEMOORG")).isPresent();
        assertThat(storeRepository.findByCode("STORE-GOLD-01")).isPresent();
        assertThat(storeRepository.findByCode("STORE-GRC-01")).isPresent();
        assertThat(storeRepository.findByCode("STORE-GEN-01")).isPresent();

        Long goldStoreId = storeRepository.findByCode("STORE-GOLD-01").orElseThrow().getId();
        assertThat(terminalRepository.findByStoreIdAndCode(goldStoreId, "TERM-01")).isPresent();
    }
}
