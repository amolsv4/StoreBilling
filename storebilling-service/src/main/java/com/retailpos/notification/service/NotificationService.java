package com.retailpos.notification.service;

import com.retailpos.notification.entity.NotificationChannel;

/**
 * Abstraction over an actual notification provider (email/SMS/WhatsApp gateway). This phase ships
 * only a logging/no-op implementation - no real provider wiring (that needs vendor selection and
 * real credentials, which belong in environment configuration, never in chat/code).
 */
public interface NotificationService {

    /**
     * Attempts to send a notification. {@code recipient} is the RAW contact detail (a real
     * provider implementation would need it to actually send) - callers are responsible for only
     * ever persisting/logging the {@link ContactMasker#mask masked} form.
     *
     * @return true if the send "succeeded" (the no-op stub always returns true).
     */
    boolean send(NotificationChannel channel, String recipient);
}
