package com.retailpos.notification.entity;

import com.retailpos.sale.entity.Sale;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Append-only record of a single receipt-notification attempt (same shape as
 * {@code com.retailpos.audit.entity.AuditLog} - no {@code updated_*}/{@code version} columns,
 * rows are never modified after insert). {@code recipient} stores ONLY the masked/truncated
 * contact detail (e.g. {@code "jo***@example.com"}, {@code "******1234"}) - the raw email/mobile
 * is never written here, per the PII-masking policy.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "notification_log")
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private NotificationChannel channel;

    /** Masked/truncated only - see class javadoc. */
    @Column(name = "recipient", length = 100)
    private String recipient;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatus status;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;
}
