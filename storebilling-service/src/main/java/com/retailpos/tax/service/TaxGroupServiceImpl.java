package com.retailpos.tax.service;

import com.retailpos.common.exception.BusinessException;
import com.retailpos.common.exception.DuplicateResourceException;
import com.retailpos.common.exception.ResourceNotFoundException;
import com.retailpos.organization.entity.Organization;
import com.retailpos.organization.repository.OrganizationRepository;
import com.retailpos.tax.dto.CreateTaxGroupRequest;
import com.retailpos.tax.dto.TaxComponentResponse;
import com.retailpos.tax.dto.TaxGroupResponse;
import com.retailpos.tax.dto.UpdateTaxGroupRequest;
import com.retailpos.tax.entity.TaxComponent;
import com.retailpos.tax.entity.TaxGroup;
import com.retailpos.tax.entity.TaxGroupComponent;
import com.retailpos.tax.mapper.TaxComponentMapper;
import com.retailpos.tax.mapper.TaxGroupMapper;
import com.retailpos.tax.repository.TaxComponentRepository;
import com.retailpos.tax.repository.TaxGroupComponentRepository;
import com.retailpos.tax.repository.TaxGroupRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
public class TaxGroupServiceImpl implements TaxGroupService {

    private final TaxGroupRepository taxGroupRepository;
    private final TaxComponentRepository taxComponentRepository;
    private final TaxGroupComponentRepository taxGroupComponentRepository;
    private final OrganizationRepository organizationRepository;
    private final TaxGroupMapper taxGroupMapper;
    private final TaxComponentMapper taxComponentMapper;

    public TaxGroupServiceImpl(TaxGroupRepository taxGroupRepository,
                                TaxComponentRepository taxComponentRepository,
                                TaxGroupComponentRepository taxGroupComponentRepository,
                                OrganizationRepository organizationRepository,
                                TaxGroupMapper taxGroupMapper,
                                TaxComponentMapper taxComponentMapper) {
        this.taxGroupRepository = taxGroupRepository;
        this.taxComponentRepository = taxComponentRepository;
        this.taxGroupComponentRepository = taxGroupComponentRepository;
        this.organizationRepository = organizationRepository;
        this.taxGroupMapper = taxGroupMapper;
        this.taxComponentMapper = taxComponentMapper;
    }

    @Override
    @Transactional
    public TaxGroupResponse create(CreateTaxGroupRequest request) {
        if (taxGroupRepository.existsByOrganizationIdAndCode(request.organizationId(), request.code())) {
            throw new DuplicateResourceException(
                    "A tax group with code '" + request.code() + "' already exists for this organization.");
        }
        Organization organization = organizationRepository.findById(request.organizationId())
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Organization", request.organizationId()));

        TaxGroup taxGroup = taxGroupMapper.toEntity(request);
        taxGroup.setOrganization(organization);
        TaxGroup saved = taxGroupRepository.save(taxGroup);
        assignComponents(saved, request.componentIds());
        return toResponseWithComponents(saved);
    }

    @Override
    @Transactional
    public TaxGroupResponse update(Long id, UpdateTaxGroupRequest request) {
        TaxGroup taxGroup = getEntity(id);
        taxGroupMapper.updateEntityFromRequest(request, taxGroup);
        if (request.active() != null) {
            taxGroup.setActive(request.active());
        }
        if (request.inclusive() != null) {
            taxGroup.setInclusive(request.inclusive());
        }
        TaxGroup saved = taxGroupRepository.save(taxGroup);
        assignComponents(saved, request.componentIds());
        return toResponseWithComponents(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TaxGroupResponse getById(Long id) {
        return toResponseWithComponents(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaxGroupResponse> list(Long organizationId, Pageable pageable) {
        Page<TaxGroup> page = organizationId != null
                ? taxGroupRepository.findAllByOrganizationId(organizationId, pageable)
                : taxGroupRepository.findAll(pageable);
        return page.map(this::toResponseWithComponents);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getEffectiveTotalPercentage(Long taxGroupId) {
        TaxGroup taxGroup = getEntity(taxGroupId);
        List<TaxGroupComponent> links = taxGroupComponentRepository.findAllByTaxGroupId(taxGroup.getId());
        return sumActivePercentages(links);
    }

    /**
     * Resyncs the group's component set to exactly {@code componentIds} by deleting the existing
     * tax_group_component rows and reinserting - the same delete-then-reinsert shape used by
     * {@code RoleServiceImpl.assignPermissions} for role_permission, since tax_group_component is
     * likewise a pure join/association table.
     */
    private void assignComponents(TaxGroup taxGroup, List<Long> componentIds) {
        if (componentIds == null) {
            return;
        }
        List<TaxGroupComponent> existing = taxGroupComponentRepository.findAllByTaxGroupId(taxGroup.getId());
        if (!existing.isEmpty()) {
            taxGroupComponentRepository.deleteAll(existing);
        }
        if (componentIds.isEmpty()) {
            return;
        }
        List<TaxComponent> components = taxComponentRepository.findAllById(componentIds);
        for (TaxComponent component : components) {
            if (!component.getOrganization().getId().equals(taxGroup.getOrganization().getId())) {
                throw new BusinessException(
                        "Tax component '" + component.getCode() + "' belongs to a different organization.");
            }
            TaxGroupComponent link = new TaxGroupComponent();
            link.setTaxGroup(taxGroup);
            link.setTaxComponent(component);
            taxGroupComponentRepository.save(link);
        }
    }

    private TaxGroupResponse toResponseWithComponents(TaxGroup taxGroup) {
        List<TaxGroupComponent> links = taxGroupComponentRepository.findAllByTaxGroupId(taxGroup.getId());
        List<TaxComponent> components = links.isEmpty()
                ? Collections.emptyList()
                : links.stream().map(TaxGroupComponent::getTaxComponent).toList();

        List<TaxComponentResponse> componentResponses = components.stream().map(taxComponentMapper::toResponse).toList();
        BigDecimal totalPercentage = sumActivePercentages(links);

        TaxGroupResponse base = taxGroupMapper.toResponse(taxGroup);
        return new TaxGroupResponse(base.id(), base.organizationId(), base.organizationCode(), base.organizationName(),
                base.code(), base.name(), base.description(), componentResponses, totalPercentage, base.active(),
                taxGroup.isInclusive(), base.createdDate(), base.updatedDate());
    }

    /**
     * Shared sum-of-active-components rule, used both by {@link #toResponseWithComponents} (for
     * {@code TaxGroupResponse.totalPercentage}) and by {@link #getEffectiveTotalPercentage} (for
     * Sales' line-level tax snapshot) - a single place computes "what does this tax group's rate
     * currently add up to".
     */
    private BigDecimal sumActivePercentages(List<TaxGroupComponent> links) {
        return links.stream()
                .map(TaxGroupComponent::getTaxComponent)
                .filter(TaxComponent::isActive)
                .map(TaxComponent::getPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private TaxGroup getEntity(Long id) {
        return taxGroupRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("TaxGroup", id));
    }
}
