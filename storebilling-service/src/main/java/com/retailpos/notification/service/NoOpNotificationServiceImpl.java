package com.retailpos.notification.service;

import com.retailpos.notification.entity.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Logging-only, no-op {@link NotificationService}. Never contacts a real provider - just logs at
 * INFO (with the recipient masked, per the PII-masking policy) and reports success.
 */
@Service
public class NoOpNotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NoOpNotificationServiceImpl.class);

    @Override
    public boolean send(NotificationChannel channel, String recipient) {
        log.info("Notification stub (no real provider wired up yet) - channel={} recipient={}",
                channel, ContactMasker.mask(channel, recipient));
        return true;
    }
}
