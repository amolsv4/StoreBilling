package com.retailpos.notification.service;

import com.retailpos.notification.dto.NotificationLogResponse;
import com.retailpos.notification.entity.NotificationChannel;

/**
 * Orchestrates the {@code POST /api/v1/sales/{id}/notifications} endpoint: resolves the sale's
 * customer contact detail for the requested channel, calls the (no-op) {@link NotificationService},
 * and records a {@code notification_log} row.
 */
public interface SaleReceiptNotificationService {

    /**
     * @throws com.retailpos.common.exception.BusinessException (400) if the sale has no customer
     * (walk-in) or the customer lacks the contact field required for {@code channel}.
     */
    NotificationLogResponse notifyReceipt(Long saleId, NotificationChannel channel);
}
