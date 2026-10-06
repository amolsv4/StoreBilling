package com.retailpos.notification.entity;

/**
 * Outcome of a single notification attempt. The current no-op stub {@code NotificationService}
 * always "succeeds" (there is no real provider to fail against yet), so only {@code SENT} is
 * produced today; {@code FAILED} is reserved for when a real provider integration lands.
 */
public enum NotificationStatus {
    SENT,
    FAILED
}
