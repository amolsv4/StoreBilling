package com.retailpos.notification.dto;

import com.retailpos.notification.entity.NotificationChannel;
import com.retailpos.notification.entity.NotificationStatus;

import java.time.LocalDateTime;

/** {@code recipient} is always the masked/truncated form - see {@code ContactMasker}. */
public record NotificationLogResponse(
        Long id,
        Long saleId,
        NotificationChannel channel,
        String recipient,
        NotificationStatus status,
        LocalDateTime createdDate
) {
}
