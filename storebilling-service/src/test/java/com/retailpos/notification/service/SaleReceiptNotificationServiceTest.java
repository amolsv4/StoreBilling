package com.retailpos.notification.service;

import com.retailpos.common.exception.BusinessException;
import com.retailpos.customer.entity.Customer;
import com.retailpos.notification.entity.NotificationChannel;
import com.retailpos.notification.entity.NotificationLog;
import com.retailpos.notification.entity.NotificationStatus;
import com.retailpos.notification.repository.NotificationLogRepository;
import com.retailpos.sale.entity.Sale;
import com.retailpos.sale.repository.SaleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaleReceiptNotificationServiceTest {

    @Mock
    private SaleRepository saleRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private NotificationLogRepository notificationLogRepository;

    private SaleReceiptNotificationServiceImpl service;

    private Sale sale;

    @BeforeEach
    void setUp() {
        service = new SaleReceiptNotificationServiceImpl(saleRepository, notificationService, notificationLogRepository);
        sale = new Sale();
        sale.setId(1L);

        lenient().when(notificationLogRepository.save(any(NotificationLog.class))).thenAnswer(inv -> {
            NotificationLog log = inv.getArgument(0);
            log.setId(99L);
            return log;
        });
    }

    @Test
    void walkInSaleWithNoCustomerFailsCleanlyInsteadOf500() {
        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> service.notifyReceipt(1L, NotificationChannel.EMAIL))
                .withMessageContaining("walk-in");

        verify(notificationService, never()).send(any(), any());
    }

    @Test
    void customerMissingEmailFailsCleanlyForEmailChannel() {
        Customer customer = new Customer();
        customer.setEmail(null);
        customer.setMobile("9876543210");
        sale.setCustomer(customer);
        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));

        assertThatExceptionOfType(BusinessException.class)
                .isThrownBy(() -> service.notifyReceipt(1L, NotificationChannel.EMAIL));

        verify(notificationService, never()).send(any(), any());
    }

    @Test
    void onlyTheMaskedEmailIsPersistedToTheNotificationLogNeverTheRawValue() {
        Customer customer = new Customer();
        customer.setEmail("john@example.com");
        sale.setCustomer(customer);
        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));
        when(notificationService.send(eq(NotificationChannel.EMAIL), eq("john@example.com"))).thenReturn(true);

        var response = service.notifyReceipt(1L, NotificationChannel.EMAIL);

        ArgumentCaptor<NotificationLog> captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogRepository).save(captor.capture());
        assertThat(captor.getValue().getRecipient()).isEqualTo("jo***@example.com");
        assertThat(captor.getValue().getRecipient()).doesNotContain("john@example.com");
        assertThat(captor.getValue().getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(response.recipient()).isEqualTo("jo***@example.com");
    }

    @Test
    void onlyTheMaskedMobileIsPersistedForSmsChannel() {
        Customer customer = new Customer();
        customer.setMobile("9876543210");
        sale.setCustomer(customer);
        when(saleRepository.findById(1L)).thenReturn(Optional.of(sale));
        when(notificationService.send(eq(NotificationChannel.SMS), eq("9876543210"))).thenReturn(true);

        service.notifyReceipt(1L, NotificationChannel.SMS);

        ArgumentCaptor<NotificationLog> captor = ArgumentCaptor.forClass(NotificationLog.class);
        verify(notificationLogRepository).save(captor.capture());
        assertThat(captor.getValue().getRecipient()).isEqualTo("******3210");
        assertThat(captor.getValue().getRecipient()).doesNotContain("9876543210");
    }
}
