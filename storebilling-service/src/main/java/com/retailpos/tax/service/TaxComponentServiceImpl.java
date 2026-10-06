package com.retailpos.tax.service;

import com.retailpos.common.exception.DuplicateResourceException;
import com.retailpos.common.exception.ResourceNotFoundException;
import com.retailpos.organization.entity.Organization;
import com.retailpos.organization.repository.OrganizationRepository;
import com.retailpos.tax.dto.CreateTaxComponentRequest;
import com.retailpos.tax.dto.TaxComponentResponse;
import com.retailpos.tax.dto.UpdateTaxComponentRequest;
import com.retailpos.tax.entity.TaxComponent;
import com.retailpos.tax.mapper.TaxComponentMapper;
import com.retailpos.tax.repository.TaxComponentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TaxComponentServiceImpl implements TaxComponentService {

    private final TaxComponentRepository taxComponentRepository;
    private final OrganizationRepository organizationRepository;
    private final TaxComponentMapper taxComponentMapper;

    public TaxComponentServiceImpl(TaxComponentRepository taxComponentRepository,
                                    OrganizationRepository organizationRepository,
                                    TaxComponentMapper taxComponentMapper) {
        this.taxComponentRepository = taxComponentRepository;
        this.organizationRepository = organizationRepository;
        this.taxComponentMapper = taxComponentMapper;
    }

    @Override
    @Transactional
    public TaxComponentResponse create(CreateTaxComponentRequest request) {
        if (taxComponentRepository.existsByOrganizationIdAndCode(request.organizationId(), request.code())) {
            throw new DuplicateResourceException(
                    "A tax component with code '" + request.code() + "' already exists for this organization.");
        }
        Organization organization = organizationRepository.findById(request.organizationId())
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Organization", request.organizationId()));

        TaxComponent taxComponent = taxComponentMapper.toEntity(request);
        taxComponent.setOrganization(organization);
        TaxComponent saved = taxComponentRepository.save(taxComponent);
        return taxComponentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TaxComponentResponse update(Long id, UpdateTaxComponentRequest request) {
        TaxComponent taxComponent = getEntity(id);
        taxComponentMapper.updateEntityFromRequest(request, taxComponent);
        if (request.active() != null) {
            taxComponent.setActive(request.active());
        }
        TaxComponent saved = taxComponentRepository.save(taxComponent);
        return taxComponentMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TaxComponentResponse getById(Long id) {
        return taxComponentMapper.toResponse(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaxComponentResponse> list(Long organizationId, Pageable pageable) {
        if (organizationId != null) {
            return taxComponentRepository.findAllByOrganizationId(organizationId, pageable)
                    .map(taxComponentMapper::toResponse);
        }
        return taxComponentRepository.findAll(pageable).map(taxComponentMapper::toResponse);
    }

    private TaxComponent getEntity(Long id) {
        return taxComponentRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("TaxComponent", id));
    }
}
