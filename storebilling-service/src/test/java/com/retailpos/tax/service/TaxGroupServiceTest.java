package com.retailpos.tax.service;

import com.retailpos.organization.entity.Organization;
import com.retailpos.organization.repository.OrganizationRepository;
import com.retailpos.tax.dto.TaxComponentResponse;
import com.retailpos.tax.dto.TaxGroupResponse;
import com.retailpos.tax.entity.TaxComponent;
import com.retailpos.tax.entity.TaxGroup;
import com.retailpos.tax.entity.TaxGroupComponent;
import com.retailpos.tax.entity.TaxType;
import com.retailpos.tax.mapper.TaxComponentMapper;
import com.retailpos.tax.mapper.TaxGroupMapper;
import com.retailpos.tax.repository.TaxComponentRepository;
import com.retailpos.tax.repository.TaxGroupComponentRepository;
import com.retailpos.tax.repository.TaxGroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaxGroupServiceTest {

    @Mock
    private TaxGroupRepository taxGroupRepository;
    @Mock
    private TaxComponentRepository taxComponentRepository;
    @Mock
    private TaxGroupComponentRepository taxGroupComponentRepository;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private TaxGroupMapper taxGroupMapper;
    @Mock
    private TaxComponentMapper taxComponentMapper;

    private TaxGroupServiceImpl taxGroupService;

    private TaxGroup group;
    private TaxComponent cgst;
    private TaxComponent sgst;

    @BeforeEach
    void setUp() {
        taxGroupService = new TaxGroupServiceImpl(taxGroupRepository, taxComponentRepository,
                taxGroupComponentRepository, organizationRepository, taxGroupMapper, taxComponentMapper);

        Organization organization = new Organization();
        organization.setId(100L);
        organization.setCode("DEMOORG");
        organization.setName("Demo Org");

        group = new TaxGroup();
        group.setId(1L);
        group.setOrganization(organization);
        group.setCode("TAXGRP-GST18");
        group.setName("GST 18%");
        group.setActive(true);

        cgst = new TaxComponent();
        cgst.setId(10L);
        cgst.setOrganization(organization);
        cgst.setCode("TAX-CGST-9");
        cgst.setTaxType(TaxType.CGST);
        cgst.setPercentage(new BigDecimal("9.00"));
        cgst.setActive(true);

        sgst = new TaxComponent();
        sgst.setId(11L);
        sgst.setOrganization(organization);
        sgst.setCode("TAX-SGST-9");
        sgst.setTaxType(TaxType.SGST);
        sgst.setPercentage(new BigDecimal("9.00"));
        sgst.setActive(true);

        when(taxGroupRepository.findById(1L)).thenReturn(Optional.of(group));
        TaxGroupResponse baseResponse = new TaxGroupResponse(1L, 100L, "DEMOORG", "Demo Org",
                "TAXGRP-GST18", "GST 18%", null, null, null, true, false, null, null);
        // lenient: getEffectiveTotalPercentageUsesTheSameSharedHelperAsGetById exercises a path
        // (getEffectiveTotalPercentage) that doesn't go through toResponseWithComponents, so it
        // never touches these two mapper stubs.
        lenient().when(taxGroupMapper.toResponse(group)).thenReturn(baseResponse);
        lenient().when(taxComponentMapper.toResponse(org.mockito.ArgumentMatchers.any(TaxComponent.class)))
                .thenAnswer(invocation -> {
                    TaxComponent component = invocation.getArgument(0);
                    return new TaxComponentResponse(component.getId(), 100L, "DEMOORG", "Demo Org",
                            component.getCode(), component.getName(), component.getTaxType(),
                            component.getPercentage(), component.isActive(), null, null);
                });
    }

    private TaxGroupComponent link(TaxComponent component) {
        TaxGroupComponent link = new TaxGroupComponent();
        link.setTaxGroup(group);
        link.setTaxComponent(component);
        return link;
    }

    @Test
    void totalPercentageSumsAllActiveComponents() {
        when(taxGroupComponentRepository.findAllByTaxGroupId(1L)).thenReturn(List.of(link(cgst), link(sgst)));

        TaxGroupResponse response = taxGroupService.getById(1L);

        assertThat(response.totalPercentage()).isEqualByComparingTo(new BigDecimal("18.00"));
        assertThat(response.components()).hasSize(2);
    }

    @Test
    void getEffectiveTotalPercentageUsesTheSameSharedHelperAsGetById() {
        when(taxGroupComponentRepository.findAllByTaxGroupId(1L)).thenReturn(List.of(link(cgst), link(sgst)));

        BigDecimal total = taxGroupService.getEffectiveTotalPercentage(1L);

        assertThat(total).isEqualByComparingTo(new BigDecimal("18.00"));
    }

    @Test
    void deactivatingAComponentReducesTheComputedTotal() {
        sgst.setActive(false);
        when(taxGroupComponentRepository.findAllByTaxGroupId(1L)).thenReturn(List.of(link(cgst), link(sgst)));

        TaxGroupResponse response = taxGroupService.getById(1L);

        // The inactive SGST component is still linked/returned in "components", but excluded from
        // the sum - matching the "filter(TaxComponent::isActive)" reduction rule.
        assertThat(response.totalPercentage()).isEqualByComparingTo(new BigDecimal("9.00"));
        assertThat(response.components()).hasSize(2);
    }
}
