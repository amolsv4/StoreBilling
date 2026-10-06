package com.retailpos.terminal.service;

import com.retailpos.common.exception.DuplicateResourceException;
import com.retailpos.common.exception.ResourceNotFoundException;
import com.retailpos.store.repository.StoreRepository;
import com.retailpos.terminal.dto.CreateTerminalRequest;
import com.retailpos.terminal.entity.TerminalType;
import com.retailpos.terminal.mapper.TerminalMapper;
import com.retailpos.terminal.repository.TerminalRepository;
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
class TerminalServiceTest {

    @Mock
    private TerminalRepository terminalRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private TerminalMapper terminalMapper;

    private TerminalServiceImpl terminalService;

    @BeforeEach
    void setUp() {
        terminalService = new TerminalServiceImpl(terminalRepository, storeRepository, terminalMapper);
    }

    @Test
    void createRejectsDuplicateCodeWithinSameStore() {
        when(terminalRepository.existsByStoreIdAndCode(1L, "TERM-01")).thenReturn(true);
        CreateTerminalRequest request = new CreateTerminalRequest(1L, "TERM-01", "Terminal 1", TerminalType.POS, null);

        assertThatExceptionOfType(DuplicateResourceException.class)
                .isThrownBy(() -> terminalService.create(request));

        verify(terminalRepository, never()).save(any());
        verify(storeRepository, never()).findById(any());
    }

    @Test
    void createThrowsResourceNotFoundWhenStoreMissing() {
        when(terminalRepository.existsByStoreIdAndCode(1L, "TERM-01")).thenReturn(false);
        when(storeRepository.findById(1L)).thenReturn(Optional.empty());
        CreateTerminalRequest request = new CreateTerminalRequest(1L, "TERM-01", "Terminal 1", TerminalType.POS, null);

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> terminalService.create(request));
    }

    @Test
    void createRequestValidationRejectsMissingRequiredFields() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            CreateTerminalRequest invalid = new CreateTerminalRequest(null, "", "", null, null);

            Set<ConstraintViolation<CreateTerminalRequest>> violations = validator.validate(invalid);

            assertThat(violations).isNotEmpty();
            assertThat(violations)
                    .extracting(v -> v.getPropertyPath().toString())
                    .contains("storeId", "code", "name", "terminalType");
        }
    }
}
