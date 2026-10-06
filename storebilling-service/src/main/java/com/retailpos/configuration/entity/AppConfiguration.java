package com.retailpos.configuration.entity;

import com.retailpos.common.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A single scoped configuration value. {@code scopeId} is intentionally polymorphic and carries
 * NO database foreign key - its meaning depends on {@code scopeType} and is validated in the
 * service layer against the corresponding table (business_type / organization / store /
 * terminal). GLOBAL scope uses the sentinel {@code scopeId = 0}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "app_configuration")
public class AppConfiguration extends BaseAuditableEntity {

    /** Sentinel scope id used for GLOBAL-scoped rows (GLOBAL has no natural owning id). */
    public static final long GLOBAL_SCOPE_ID = 0L;

    @Column(name = "config_key", nullable = false, length = 100)
    private String configKey;

    @Column(name = "config_value", nullable = false, length = 1000)
    private String configValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "data_type", nullable = false, length = 20)
    private DataType dataType;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false, length = 20)
    private ScopeType scopeType;

    @Column(name = "scope_id", nullable = false)
    private Long scopeId;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
