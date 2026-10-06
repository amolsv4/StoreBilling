package com.retailpos.user.runner;

import com.retailpos.role.entity.Role;
import com.retailpos.role.entity.UserRole;
import com.retailpos.role.repository.RoleRepository;
import com.retailpos.role.repository.UserRoleRepository;
import com.retailpos.user.entity.User;
import com.retailpos.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates the very first SUPER_ADMIN user on startup, from {@code BOOTSTRAP_ADMIN_USERNAME} /
 * {@code BOOTSTRAP_ADMIN_PASSWORD} environment variables - deliberately NOT from any seeded
 * password or literal default, per security policy ("never hardcode credentials/keys, including
 * {@code @Value} defaults that are real-looking passwords").
 *
 * <p>The {@code :} empty-string defaults below are sentinels that force an explicit environment
 * variable, not real fallback credentials. If no SUPER_ADMIN user exists yet and either variable
 * is blank, startup fails fast with an actionable log message rather than silently starting an
 * un-administrable (or worse, guessably-administrable) system.
 */
@Component
public class SuperAdminBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SuperAdminBootstrapRunner.class);
    private static final String SUPER_ADMIN_ROLE_CODE = "SUPER_ADMIN";

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${BOOTSTRAP_ADMIN_USERNAME:}")
    private String bootstrapUsername;

    @Value("${BOOTSTRAP_ADMIN_PASSWORD:}")
    private String bootstrapPassword;

    @Value("${BOOTSTRAP_ADMIN_EMAIL:}")
    private String bootstrapEmail;

    public SuperAdminBootstrapRunner(UserRepository userRepository,
                                      UserRoleRepository userRoleRepository,
                                      RoleRepository roleRepository,
                                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRoleRepository.existsByRoleCode(SUPER_ADMIN_ROLE_CODE)) {
            log.info("A SUPER_ADMIN user already exists - skipping bootstrap.");
            return;
        }

        if (isBlank(bootstrapUsername) || isBlank(bootstrapPassword)) {
            log.error("No SUPER_ADMIN user exists yet and BOOTSTRAP_ADMIN_USERNAME / BOOTSTRAP_ADMIN_PASSWORD "
                    + "environment variables are not both set. Refusing to start with no way to administer the "
                    + "system. Set both environment variables (see .env.example) and restart.");
            throw new IllegalStateException(
                    "BOOTSTRAP_ADMIN_USERNAME and BOOTSTRAP_ADMIN_PASSWORD must both be set to bootstrap the "
                            + "first SUPER_ADMIN user.");
        }

        Role superAdminRole = roleRepository.findByCode(SUPER_ADMIN_ROLE_CODE)
                .orElseThrow(() -> new IllegalStateException(
                        "SUPER_ADMIN role not found in the database. Ensure the reference-data Flyway migration "
                                + "(V6, under classpath:db/seed-dev) has been applied - e.g. by activating the "
                                + "'local' Spring profile - before starting the application."));

        if (userRepository.existsByUsername(bootstrapUsername)) {
            log.warn("A user with username '{}' already exists but has no SUPER_ADMIN role; leaving it "
                    + "unmodified. Assign the SUPER_ADMIN role to it manually if this was meant to be the "
                    + "bootstrap admin.", bootstrapUsername);
            return;
        }

        String email = isBlank(bootstrapEmail) ? bootstrapUsername + "@storebilling.local" : bootstrapEmail;

        User user = new User();
        user.setUsername(bootstrapUsername);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(bootstrapPassword));
        user.setFirstName("Bootstrap");
        user.setLastName("Administrator");
        user.setAllStoreAccess(true);
        user.setActive(true);
        user.setAccountLocked(false);
        User savedUser = userRepository.save(user);

        UserRole userRole = new UserRole();
        userRole.setUser(savedUser);
        userRole.setRole(superAdminRole);
        userRoleRepository.save(userRole);

        log.warn("Bootstrapped initial SUPER_ADMIN user '{}'. Log in and rotate its password (or create named "
                + "admin accounts and retire this one) as soon as possible.", bootstrapUsername);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
