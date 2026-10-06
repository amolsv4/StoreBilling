package com.retailpos.organization.entity;

import com.retailpos.common.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Independent global reference lookup (GOLD / GROCERY / GENERAL) - not organization-owned.
 * Phase 1 treats business types purely as reference rows; no product/catalog schema hangs off
 * this entity.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "business_type")
public class BusinessType extends BaseAuditableEntity {

    @Column(name = "code", nullable = false, length = 30, unique = true)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
