package com.retailpos.notification.entity;

/** Receipt notification channel. Stored as VARCHAR(20) via {@code @Enumerated(EnumType.STRING)}. */
public enum NotificationChannel {
    EMAIL,
    SMS,
    WHATSAPP
}
