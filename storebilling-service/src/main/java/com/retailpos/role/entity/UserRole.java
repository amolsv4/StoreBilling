package com.retailpos.role.entity;

import com.retailpos.common.entity.BaseAuditableEntity;
import com.retailpos.user.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Join entity assigning a {@link Role} to a {@link User}. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "user_role")
public class UserRole extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
}
