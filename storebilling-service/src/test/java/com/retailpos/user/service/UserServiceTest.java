package com.retailpos.user.service;

import com.retailpos.common.exception.DuplicateResourceException;
import com.retailpos.organization.repository.OrganizationRepository;
import com.retailpos.role.repository.RoleRepository;
import com.retailpos.role.repository.UserRoleRepository;
import com.retailpos.store.repository.StoreRepository;
import com.retailpos.user.dto.CreateUserRequest;
import com.retailpos.user.mapper.UserMapper;
import com.retailpos.user.repository.UserRepository;
import com.retailpos.user.repository.UserStoreAccessRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private UserRoleRepository userRoleRepository;
    @Mock
    private UserStoreAccessRepository userStoreAccessRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, organizationRepository, storeRepository, roleRepository,
                userRoleRepository, userStoreAccessRepository, userMapper, passwordEncoder);
    }

    @Test
    void createRejectsDuplicateUsername() {
        when(userRepository.existsByUsername("jdoe")).thenReturn(true);
        CreateUserRequest request = new CreateUserRequest(
                "jdoe", "jdoe@example.com", "SuperSecret1!", "John", "Doe", null, null, false, null, null);

        assertThatExceptionOfType(DuplicateResourceException.class)
                .isThrownBy(() -> userService.create(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    void createRejectsDuplicateEmailWhenUsernameIsFree() {
        when(userRepository.existsByUsername("jdoe")).thenReturn(false);
        when(userRepository.existsByEmail("jdoe@example.com")).thenReturn(true);
        CreateUserRequest request = new CreateUserRequest(
                "jdoe", "jdoe@example.com", "SuperSecret1!", "John", "Doe", null, null, false, null, null);

        assertThatExceptionOfType(DuplicateResourceException.class)
                .isThrownBy(() -> userService.create(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    void createRequestValidationRejectsMissingRequiredFields() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            CreateUserRequest invalid = new CreateUserRequest(
                    "", "not-an-email", "short", "", "", null, null, null, List.of(), List.of());

            Set<ConstraintViolation<CreateUserRequest>> violations = validator.validate(invalid);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("username", "email", "password", "firstName", "lastName", "allStoreAccess");
        }
    }
}
