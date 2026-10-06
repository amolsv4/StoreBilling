package com.retailpos.sale.service;

import com.retailpos.sale.entity.InvoiceSequence;
import com.retailpos.sale.repository.InvoiceSequenceRepository;
import com.retailpos.store.entity.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceNumberServiceTest {

    @Mock
    private InvoiceSequenceRepository invoiceSequenceRepository;

    private InvoiceNumberServiceImpl invoiceNumberService;

    private Store store;

    @BeforeEach
    void setUp() {
        invoiceNumberService = new InvoiceNumberServiceImpl(invoiceSequenceRepository);

        store = new Store();
        store.setId(100L);
        store.setCode("STORE1");
    }

    @Test
    void formatMatchesStoreCodeDateAndSixDigitSequence() {
        InvoiceSequence sequence = new InvoiceSequence();
        sequence.setStoreId(100L);
        sequence.setNextValue(1L);
        when(invoiceSequenceRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(sequence));

        String invoiceNumber = invoiceNumberService.generateNextInvoiceNumber(store);

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        assertThat(invoiceNumber).isEqualTo("STORE1-" + today + "-000001");
        assertThat(invoiceNumber).matches("^[A-Za-z0-9]+-\\d{8}-\\d{6}$");

        verify(invoiceSequenceRepository).upsertNoOp(100L);
    }

    @Test
    void eachCallIncrementsTheStoredNextValueBeforeSaving() {
        InvoiceSequence sequence = new InvoiceSequence();
        sequence.setStoreId(100L);
        sequence.setNextValue(1L);
        when(invoiceSequenceRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(sequence));

        invoiceNumberService.generateNextInvoiceNumber(store);

        ArgumentCaptor<InvoiceSequence> captor = ArgumentCaptor.forClass(InvoiceSequence.class);
        verify(invoiceSequenceRepository).save(captor.capture());
        // The assigned value (1) is used in the formatted number; the stored counter is advanced
        // to 2 for the *next* call - this is what guarantees two sequential completions never
        // reuse the same number, and (combined with the PESSIMISTIC_WRITE lock the real
        // repository finder takes) that two concurrent completions under the same store don't
        // either.
        assertThat(captor.getValue().getNextValue()).isEqualTo(2L);
    }

    @Test
    void sequentialCallsProduceIncrementingInvoiceNumbersPerStore() {
        InvoiceSequence sequence = new InvoiceSequence();
        sequence.setStoreId(100L);
        sequence.setNextValue(1L);
        when(invoiceSequenceRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(sequence));

        String first = invoiceNumberService.generateNextInvoiceNumber(store);
        // Simulate the persisted row having advanced to 2 for the store's second completion.
        sequence.setNextValue(2L);
        String second = invoiceNumberService.generateNextInvoiceNumber(store);

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        assertThat(first).isEqualTo("STORE1-" + today + "-000001");
        assertThat(second).isEqualTo("STORE1-" + today + "-000002");
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void missingSequenceRowAfterUpsertIsATreatedAsAnIllegalState() {
        when(invoiceSequenceRepository.findByIdForUpdate(100L)).thenReturn(Optional.empty());

        org.assertj.core.api.Assertions.assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> invoiceNumberService.generateNextInvoiceNumber(store));
    }
}
