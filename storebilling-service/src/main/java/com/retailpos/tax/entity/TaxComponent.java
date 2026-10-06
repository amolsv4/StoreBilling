package com.retailpos.tax.entity;

import com.retailpos.common.entity.BaseAuditableEntity;
import com.retailpos.organization.entity.Organization;
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

import java.math.BigDecimal;

/**
 * A single named tax rate (e.g. CGST 9%), scoped to exactly one {@link Organization}.
 * {@link TaxGroup}s bundle one or more components together via {@link TaxGroupComponent}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tax_component")
public class TaxComponent extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "code", nullable = false, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_type", nullable = false, length = 20)
    private TaxType taxType;

    @Column(name = "percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal percentage;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
