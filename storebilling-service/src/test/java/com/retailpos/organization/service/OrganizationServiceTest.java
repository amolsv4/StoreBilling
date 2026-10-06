package com.retailpos.organization.service;

import com.retailpos.common.exception.DuplicateResourceException;
import com.retailpos.common.exception.ResourceNotFoundException;
import com.retailpos.organization.dto.CreateOrganizationRequest;
import com.retailpos.organization.dto.UpdateOrganizationRequest;
import com.retailpos.organization.mapper.OrganizationMapper;
import com.retailpos.organization.repository.OrganizationRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private OrganizationMapper organizationMapper;

    private OrganizationServiceImpl organizationService;

    @BeforeEach
    void setUp() {
        organizationService = new OrganizationServiceImpl(organizationRepository, organizationMapper);
    }

    @Test
    void createRejectsDuplicateCode() {
        when(organizationRepository.existsByCode("DEMOORG")).thenReturn(true);
        CreateOrganizationRequest request = new CreateOrganizationRequest(
                "DEMOORG", "Demo Retail Group", null, null, null, null, null, null, null, null, null, null);

        assertThatExceptionOfType(DuplicateResourceException.class)
                .isThrownBy(() -> organizationService.create(request));

        verify(organizationRepository, never()).save(any());
    }

    @Test
    void updateThrowsResourceNotFoundWhenMissing() {
        when(organizationRepository.findById(404L)).thenReturn(Optional.empty());
        UpdateOrganizationRequest request = new UpdateOrganizationRequest(
                "New Name", null, null, null, null, null, null, null, null, null, null, null);

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> organizationService.update(404L, request));
    }

    @Test
    void createRequestValidationRejectsMissingRequiredFields() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            CreateOrganizationRequest invalid = new CreateOrganizationRequest(
                    "", "", null, null, null, null, null, null, null, null, "not-an-email", null);

            Set<ConstraintViolation<CreateOrganizationRequest>> violations = validator.validate(invalid);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("code", "name", "contactEmail");
        }
    }
}
