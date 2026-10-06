package com.retailpos.tax.entity;

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

/**
 * Named bundle of one or more {@link TaxComponent}s (via {@link TaxGroupComponent}), scoped to
 * exactly one {@link Organization}. The combined percentage is never persisted here - it is
 * computed on demand in the service layer from the currently-linked, currently-active components.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tax_group")
public class TaxGroup extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "code", nullable = false, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    /**
     * Section 0 item 3 (Phase 3): exclusive (the default/prior behaviour) adds tax on top of the
     * entered price; inclusive treats the entered price as tax-already-included and
     * back-calculates the tax portion for display only. Branched on by Sales when snapshotting a
     * line's {@code taxAmount}/{@code lineTotal} at add-time.
     */
    @Column(name = "is_inclusive", nullable = false)
    private boolean inclusive = false;
}
