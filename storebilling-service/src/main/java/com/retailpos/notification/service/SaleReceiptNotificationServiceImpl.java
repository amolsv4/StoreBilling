package com.retailpos.notification.service;

import com.retailpos.common.exception.BusinessException;
import com.retailpos.common.exception.ResourceNotFoundException;
import com.retailpos.customer.entity.Customer;
import com.retailpos.notification.dto.NotificationLogResponse;
import com.retailpos.notification.entity.NotificationChannel;
import com.retailpos.notification.entity.NotificationLog;
import com.retailpos.notification.entity.NotificationStatus;
import com.retailpos.notification.repository.NotificationLogRepository;
import com.retailpos.sale.entity.Sale;
import com.retailpos.sale.repository.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SaleReceiptNotificationServiceImpl implements SaleReceiptNotificationService {

    private final SaleRepository saleRepository;
    private final NotificationService notificationService;
    private final NotificationLogRepository notificationLogRepository;

    public SaleReceiptNotificationServiceImpl(SaleRepository saleRepository,
                                                NotificationService notificationService,
                                                NotificationLogRepository notificationLogRepository) {
        this.saleRepository = saleRepository;
        this.notificationService = notificationService;
        this.notificationLogRepository = notificationLogRepository;
    }

    @Override
    @Transactional
    public NotificationLogResponse notifyReceipt(Long saleId, NotificationChannel channel) {
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Sale", saleId));

        Customer customer = sale.getCustomer();
        if (customer == null) {
            throw new BusinessException("This is a walk-in sale with no customer on file; a receipt cannot be sent.");
        }

        String rawContact = switch (channel) {
            case EMAIL -> customer.getEmail();
            case SMS, WHATSAPP -> customer.getMobile();
        };
        if (rawContact == null || rawContact.isBlank()) {
            String fieldName = channel == NotificationChannel.EMAIL ? "email address" : "mobile number";
            throw new BusinessException("The sale's customer has no " + fieldName + " on file for " + channel + " notifications.");
        }

        boolean sent = notificationService.send(channel, rawContact);
        String maskedRecipient = ContactMasker.mask(channel, rawContact);

        NotificationLog log = new NotificationLog();
        log.setSale(sale);
        log.setChannel(channel);
        log.setRecipient(maskedRecipient);
        log.setStatus(sent ? NotificationStatus.SENT : NotificationStatus.FAILED);
        log.setCreatedDate(LocalDateTime.now());
        NotificationLog saved = notificationLogRepository.save(log);

        return new NotificationLogResponse(saved.getId(), sale.getId(), saved.getChannel(), saved.getRecipient(),
                saved.getStatus(), saved.getCreatedDate());
    }
}
