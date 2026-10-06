package com.retailpos.notification.dto;

import com.retailpos.notification.entity.NotificationChannel;
import jakarta.validation.constraints.NotNull;

public record SendNotificationRequest(
        @NotNull NotificationChannel channel
) {
}
