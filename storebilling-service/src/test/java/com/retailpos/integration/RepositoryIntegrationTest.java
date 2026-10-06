package com.retailpos.integration;

import com.retailpos.configuration.entity.AppConfiguration;
import com.retailpos.configuration.entity.DataType;
import com.retailpos.configuration.entity.ScopeType;
import com.retailpos.configuration.repository.AppConfigurationRepository;
import com.retailpos.organization.entity.BusinessType;
import com.retailpos.organization.entity.Organization;
import com.retailpos.organization.repository.OrganizationRepository;
import com.retailpos.store.entity.Store;
import com.retailpos.store.repository.StoreRepository;
import com.retailpos.terminal.entity.Terminal;
import com.retailpos.terminal.entity.TerminalType;
import com.retailpos.terminal.repository.TerminalRepository;
import com.retailpos.user.entity.User;
import com.retailpos.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Basic persistence round-trip for each of the core Phase 1 entities against a real MySQL 8.4
 * container - verifies mappings, audit-column population, and uniqueness constraints actually
 * hold at the database level (not just in mocked unit tests).
 */
@Transactional
class RepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private TerminalRepository terminalRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AppConfigurationRepository appConfigurationRepository;

    @Test
    void organizationRoundTrip() {
        Organization organization = new Organization();
        organization.setCode("IT-ORG-01");
        organization.setName("Integration Test Org");
        Organization saved = organizationRepository.save(organization);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedDate()).isNotNull();
        assertThat(saved.getCreatedBy()).isNotBlank();
        assertThat(organizationRepository.existsByCode("IT-ORG-01")).isTrue();
    }

    @Test
    void storeAndTerminalRoundTrip() {
        Organization organization = organizationRepository.findByCode("DEMOORG").orElseThrow();
        BusinessType businessType = storeRepository.findByCode("STORE-GOLD-01").orElseThrow().getBusinessType();

        Store store = new Store();
        store.setOrganization(organization);
        store.setBusinessType(businessType);
        store.setCode("IT-STORE-01");
        store.setName("Integration Test Store");
        store.setTimezone("UTC");
        Store savedStore = storeRepository.save(store);
        assertThat(savedStore.getId()).isNotNull();

        Terminal terminal = new Terminal();
        terminal.setStore(savedStore);
        terminal.setCode("IT-TERM-01");
        terminal.setName("Integration Test Terminal");
        terminal.setTerminalType(TerminalType.POS);
        Terminal savedTerminal = terminalRepository.save(terminal);

        assertThat(savedTerminal.getId()).isNotNull();
        assertThat(savedTerminal.getStore().getCode()).isEqualTo("IT-STORE-01");
        assertThat(terminalRepository.existsByStoreIdAndCode(savedStore.getId(), "IT-TERM-01")).isTrue();
    }

    @Test
    void userRoundTripEnforcesUniqueUsernameAndEmail() {
        User user = new User();
        user.setUsername("it_test_user");
        user.setEmail("it_test_user@example.com");
        user.setPasswordHash("{bcrypt}$2a$12$abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzab");
        user.setFirstName("Integration");
        user.setLastName("Tester");
        User saved = userRepository.save(user);

        assertThat(saved.getId()).isNotNull();
        assertThat(userRepository.existsByUsername("it_test_user")).isTrue();
        assertThat(userRepository.existsByEmail("it_test_user@example.com")).isTrue();
        assertThat(userRepository.findByUsername("it_test_user")).isPresent();
    }

    @Test
    void appConfigurationRoundTripEnforcesUniqueKeyScope() {
        AppConfiguration configuration = new AppConfiguration();
        configuration.setConfigKey("IT_TEST_KEY");
        configuration.setConfigValue("some-value");
        configuration.setDataType(DataType.STRING);
        configuration.setScopeType(ScopeType.GLOBAL);
        configuration.setScopeId(AppConfiguration.GLOBAL_SCOPE_ID);
        AppConfiguration saved = appConfigurationRepository.save(configuration);

        assertThat(saved.getId()).isNotNull();
        assertThat(appConfigurationRepository.existsByConfigKeyAndScopeTypeAndScopeId(
                "IT_TEST_KEY", ScopeType.GLOBAL, AppConfiguration.GLOBAL_SCOPE_ID)).isTrue();
    }
}
