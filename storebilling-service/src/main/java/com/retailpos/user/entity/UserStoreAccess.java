package com.retailpos.user.entity;

import com.retailpos.common.entity.BaseAuditableEntity;
import com.retailpos.store.entity.Store;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Grants a {@link User} access to a specific {@link Store}. Ignored for users where
 * {@link User#isAllStoreAccess()} is {@code true}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_store_access")
public class UserStoreAccess extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;
}
