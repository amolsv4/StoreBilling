package com.retailpos.product.entity;

import com.retailpos.category.entity.Category;
import com.retailpos.common.entity.BaseAuditableEntity;
import com.retailpos.organization.entity.Organization;
import com.retailpos.tax.entity.TaxGroup;
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
 * Sellable/purchasable catalog item, scoped to exactly one {@link Organization}. Belongs to
 * exactly one {@link Category} and, optionally, one {@link TaxGroup}.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "product")
public class Product extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tax_group_id")
    private TaxGroup taxGroup;

    @Column(name = "sku", nullable = false, length = 50)
    private String sku;

    @Column(name = "barcode", length = 50)
    private String barcode;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "brand", length = 100)
    private String brand;

    @Enumerated(EnumType.STRING)
    @Column(name = "uom", nullable = false, length = 10)
    private UnitOfMeasure uom;

    @Column(name = "sale_enabled", nullable = false)
    private boolean saleEnabled = true;

    @Column(name = "purchase_enabled", nullable = false)
    private boolean purchaseEnabled = true;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;
}
