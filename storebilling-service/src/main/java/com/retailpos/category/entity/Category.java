package com.retailpos.category.entity;

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
 * Product category, scoped to exactly one {@link Organization}. Supports a self-referencing
 * parent/child hierarchy via {@link #parentCategory} (nullable - top-level categories have no
 * parent). Cycle prevention across the hierarchy is enforced in the service layer, not here.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "category")
public class Category extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_category_id")
    private Category parentCategory;

    @Column(name = "code", nullable = false, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
