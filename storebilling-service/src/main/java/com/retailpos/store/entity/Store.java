package com.retailpos.store.entity;

import com.retailpos.common.entity.BaseAuditableEntity;
import com.retailpos.organization.entity.BusinessType;
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
 * Belongs to exactly one {@link Organization} and one {@link BusinessType}.
 * Store (1) --&lt; Terminal (N).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "store")
public class Store extends BaseAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_type_id", nullable = false)
    private BusinessType businessType;

    @Column(name = "code", nullable = false, length = 30, unique = true)
    private String code;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "address_line1", length = 150)
    private String addressLine1;

    @Column(name = "address_line2", length = 150)
    private String addressLine2;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "phone", length = 30)
    private String phone;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "timezone", nullable = false, length = 50)
    private String timezone = "UTC";

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    /**
     * Plain FK column (no JPA {@code @ManyToOne} to {@code PriceList}) - deliberately avoids a
     * new {@code store -> pricing} package dependency at the entity level. Validated (exists,
     * same organization) at the service layer instead of via a JPA relationship. Used by
     * {@code PricingResolutionService} to resolve a product's effective selling price for a store.
     */
    @Column(name = "default_price_list_id")
    private Long defaultPriceListId;
}
