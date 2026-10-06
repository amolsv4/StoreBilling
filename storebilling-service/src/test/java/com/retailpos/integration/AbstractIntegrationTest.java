package com.retailpos.integration;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.UUID;

/**
 * Shared base for Testcontainers-backed integration tests: spins up a real MySQL 8.4 container
 * and points the Spring context at it, with the "local" profile active so the seed-dev Flyway
 * migrations (V6-V8) run too.
 *
 * <p>The JWT secret and bootstrap admin credentials below are randomly generated per JVM/test
 * run and only ever exist inside this ephemeral container/test context - never reused anywhere
 * else - so they are not "real" credentials within the meaning of the no-hardcoded-secrets
 * policy (comparable to Testcontainers' own throwaway root DB password).
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
public abstract class AbstractIntegrationTest {

    protected static final String TEST_BOOTSTRAP_ADMIN_USERNAME = "it_bootstrap_admin";
    protected static final String TEST_BOOTSTRAP_ADMIN_PASSWORD =
            UUID.randomUUID() + "-" + UUID.randomUUID();
    private static final String TEST_JWT_SECRET = UUID.randomUUID() + "" + UUID.randomUUID() + "" + UUID.randomUUID();

    // The @Testcontainers JUnit 5 extension starts/stops this container automatically (via the
    // @Container annotation) - static analysis doesn't see that lifecycle management and flags it
    // as an unclosed resource, but it's not actually leaked.
    @SuppressWarnings("resource")
    @Container
    static final MySQLContainer MYSQL_CONTAINER = new MySQLContainer(DockerImageName.parse("mysql:8.4"))
            .withDatabaseName("storebilling_it")
            .withUsername("storebilling_it")
            .withPassword(UUID.randomUUID().toString());

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
        registry.add("JWT_SECRET", () -> TEST_JWT_SECRET);
        registry.add("BOOTSTRAP_ADMIN_USERNAME", () -> TEST_BOOTSTRAP_ADMIN_USERNAME);
        registry.add("BOOTSTRAP_ADMIN_PASSWORD", () -> TEST_BOOTSTRAP_ADMIN_PASSWORD);
    }
}
