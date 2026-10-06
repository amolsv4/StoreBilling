package com.retailpos.tax.entity;

import com.retailpos.common.entity.BaseAuditableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Join entity assigning a {@link TaxComponent} to a {@link TaxGroup}. */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "tax_group_component")
public class TaxGroupComponent extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_group_id", nullable = false)
    private TaxGroup taxGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_component_id", nullable = false)
    private TaxComponent taxComponent;
}
