package com.retailpos.notification.service;

import com.retailpos.notification.entity.NotificationChannel;

/**
 * Shared PII-masking rule for contact details, used both by the no-op {@code NotificationService}
 * (for its INFO log line) and by {@code SaleReceiptNotificationServiceImpl} (for the value
 * persisted to {@code notification_log}) - a single place decides the masking so the two never
 * drift apart. Examples: {@code "john@example.com"} -&gt; {@code "jo***@example.com"};
 * {@code "9876543210"} -&gt; {@code "******3210"}.
 */
public final class ContactMasker {

    private ContactMasker() {
    }

    public static String mask(NotificationChannel channel, String rawContact) {
        if (rawContact == null || rawContact.isBlank()) {
            return rawContact;
        }
        return channel == NotificationChannel.EMAIL ? maskEmail(rawContact) : maskPhone(rawContact);
    }

    private static String maskEmail(String email) {
        int at = email.indexOf('@');
        if (at <= 0) {
            // Not a well-formed email (shouldn't happen for validated Customer.email) - fall back
            // to phone-style masking rather than leaking the whole raw value.
            return maskPhone(email);
        }
        String local = email.substring(0, at);
        String domain = email.substring(at);
        String visible = local.length() >= 2 ? local.substring(0, 2) : local;
        return visible + "***" + domain;
    }

    private static String maskPhone(String phone) {
        String trimmed = phone.trim();
        int keep = Math.min(4, trimmed.length());
        String lastDigits = trimmed.substring(trimmed.length() - keep);
        String stars = "*".repeat(Math.max(0, trimmed.length() - keep));
        return stars + lastDigits;
    }
}
