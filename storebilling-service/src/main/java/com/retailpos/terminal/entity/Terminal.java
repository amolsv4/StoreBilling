package com.retailpos.terminal.entity;

import com.retailpos.common.entity.BaseAuditableEntity;
import com.retailpos.store.entity.Store;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Belongs to exactly one {@link Store}. Terminal intentionally has no {@code business_type_id}
 * column - it inherits business type transitively via {@code terminal -> store ->
 * store.businessType}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "terminal")
public class Terminal extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "code", nullable = false, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "terminal_type", nullable = false, length = 20)
    private TerminalType terminalType;

    @Column(name = "device_identifier", length = 100)
    private String deviceIdentifier;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
