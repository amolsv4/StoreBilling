package com.retailpos.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Common id + audit columns shared by (almost) every entity: {@code id}, {@code createdBy},
 * {@code createdDate}, {@code updatedBy}, {@code updatedDate}, and an optimistic-locking
 * {@code version}.
 *
 * <p>{@code createdBy}/{@code updatedBy} store the acting username as plain text (no FK to
 * app_user) - this avoids a bootstrap circular dependency, since the very first user must be
 * creatable/auditable before any app_user row exists.
 *
 * <p>Note: {@link com.retailpos.audit.entity.AuditLog} deliberately does NOT extend this class -
 * it is an append-only table with no updated_by/updated_date/version columns.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false, length = 50)
    private String createdBy;

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedBy
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    @LastModifiedDate
    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
