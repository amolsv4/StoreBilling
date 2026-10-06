package com.retailpos.user.entity;

import com.retailpos.common.entity.BaseAuditableEntity;
import com.retailpos.organization.entity.Organization;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Application user. Mapped to table {@code app_user} because {@code user} is a MySQL 8.4
 * reserved word.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "app_user")
public class User extends BaseAuditableEntity {

    @Column(name = "username", nullable = false, length = 50, unique = true)
    private String username;

    @Column(name = "email", nullable = false, length = 150, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "phone", length = 30)
    private String phone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(name = "is_all_store_access", nullable = false)
    private boolean allStoreAccess = false;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "is_account_locked", nullable = false)
    private boolean accountLocked = false;

    @Column(name = "last_login_date")
    private LocalDateTime lastLoginDate;
}
