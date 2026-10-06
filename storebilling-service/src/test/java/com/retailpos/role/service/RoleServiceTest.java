package com.retailpos.role.service;

import com.retailpos.common.exception.DuplicateResourceException;
import com.retailpos.common.exception.ResourceNotFoundException;
import com.retailpos.role.dto.CreateRoleRequest;
import com.retailpos.role.mapper.RoleMapper;
import com.retailpos.role.repository.PermissionRepository;
import com.retailpos.role.repository.RolePermissionRepository;
import com.retailpos.role.repository.RoleRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private RolePermissionRepository rolePermissionRepository;
    @Mock
    private RoleMapper roleMapper;

    private RoleServiceImpl roleService;

    @BeforeEach
    void setUp() {
        roleService = new RoleServiceImpl(roleRepository, permissionRepository, rolePermissionRepository, roleMapper);
    }

    @Test
    void createRejectsDuplicateCode() {
        when(roleRepository.existsByCode("SUPER_ADMIN")).thenReturn(true);
        CreateRoleRequest request = new CreateRoleRequest("SUPER_ADMIN", "Super Admin", null, List.of());

        assertThatExceptionOfType(DuplicateResourceException.class)
                .isThrownBy(() -> roleService.create(request));

        verify(roleRepository, never()).save(any());
    }

    @Test
    void getByIdThrowsResourceNotFoundWhenMissing() {
        when(roleRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> roleService.getById(404L));
    }

    @Test
    void createRequestValidationRejectsMissingRequiredFields() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            CreateRoleRequest invalid = new CreateRoleRequest("", "", null, null);

            Set<ConstraintViolation<CreateRoleRequest>> violations = validator.validate(invalid);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("code", "name");
        }
    }
}
