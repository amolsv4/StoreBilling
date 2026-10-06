package com.retailpos.notification.service;

import com.retailpos.notification.entity.NotificationChannel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContactMaskerTest {

    @Test
    void masksEmailKeepingFirstTwoLocalCharsAndTheFullDomain() {
        assertThat(ContactMasker.mask(NotificationChannel.EMAIL, "john@example.com")).isEqualTo("jo***@example.com");
    }

    @Test
    void masksPhoneKeepingOnlyLastFourDigits() {
        assertThat(ContactMasker.mask(NotificationChannel.SMS, "9876543210")).isEqualTo("******3210");
        assertThat(ContactMasker.mask(NotificationChannel.WHATSAPP, "9876543210")).isEqualTo("******3210");
    }

    @Test
    void neverLeaksTheRawValueForEitherChannel() {
        String maskedEmail = ContactMasker.mask(NotificationChannel.EMAIL, "jane.doe@example.com");
        String maskedPhone = ContactMasker.mask(NotificationChannel.SMS, "5551234567");

        assertThat(maskedEmail).doesNotContain("jane.doe@example.com");
        assertThat(maskedPhone).doesNotContain("5551234567");
    }
}
