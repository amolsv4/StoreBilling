package com.retailpos.store.service;

import com.retailpos.common.exception.DuplicateResourceException;
import com.retailpos.common.exception.ResourceNotFoundException;
import com.retailpos.organization.repository.BusinessTypeRepository;
import com.retailpos.organization.repository.OrganizationRepository;
import com.retailpos.pricing.repository.PriceListRepository;
import com.retailpos.store.dto.CreateStoreRequest;
import com.retailpos.store.mapper.StoreMapper;
import com.retailpos.store.repository.StoreRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private BusinessTypeRepository businessTypeRepository;
    @Mock
    private PriceListRepository priceListRepository;
    @Mock
    private StoreMapper storeMapper;

    private StoreServiceImpl storeService;

    @BeforeEach
    void setUp() {
        storeService = new StoreServiceImpl(storeRepository, organizationRepository, businessTypeRepository,
                priceListRepository, storeMapper);
    }

    @Test
    void createRejectsDuplicateCode() {
        when(storeRepository.existsByCode("STORE-GOLD-01")).thenReturn(true);
        CreateStoreRequest request = new CreateStoreRequest(
                1L, 1L, "STORE-GOLD-01", "Gold Store", null, null, null, null, null, null, null, null, "UTC");

        assertThatExceptionOfType(DuplicateResourceException.class)
                .isThrownBy(() -> storeService.create(request));

        verify(storeRepository, never()).save(any());
        verify(organizationRepository, never()).findById(any());
    }

    @Test
    void getByIdThrowsResourceNotFoundWhenMissing() {
        when(storeRepository.findById(404L)).thenReturn(java.util.Optional.empty());

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> storeService.getById(404L));
    }

    @Test
    void createRequestValidationRejectsMissingRequiredFields() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            CreateStoreRequest invalid = new CreateStoreRequest(
                    null, null, "", "", null, null, null, null, null, null, null, "not-an-email", null);

            Set<ConstraintViolation<CreateStoreRequest>> violations = validator.validate(invalid);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("organizationId", "businessTypeId", "code", "name", "email");
        }
    }
}
