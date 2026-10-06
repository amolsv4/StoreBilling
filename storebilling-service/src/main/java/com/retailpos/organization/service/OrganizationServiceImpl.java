package com.retailpos.organization.service;

import com.retailpos.common.exception.DuplicateResourceException;
import com.retailpos.common.exception.ResourceNotFoundException;
import com.retailpos.organization.dto.CreateOrganizationRequest;
import com.retailpos.organization.dto.OrganizationResponse;
import com.retailpos.organization.dto.UpdateOrganizationRequest;
import com.retailpos.organization.entity.Organization;
import com.retailpos.organization.mapper.OrganizationMapper;
import com.retailpos.organization.repository.OrganizationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;

    public OrganizationServiceImpl(OrganizationRepository organizationRepository, OrganizationMapper organizationMapper) {
        this.organizationRepository = organizationRepository;
        this.organizationMapper = organizationMapper;
    }

    @Override
    @Transactional
    public OrganizationResponse create(CreateOrganizationRequest request) {
        if (organizationRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("An organization with code '" + request.code() + "' already exists.");
        }
        Organization organization = organizationMapper.toEntity(request);
        Organization saved = organizationRepository.save(organization);
        return organizationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public OrganizationResponse update(Long id, UpdateOrganizationRequest request) {
        Organization organization = getEntity(id);
        organizationMapper.updateEntityFromRequest(request, organization);
        if (request.active() != null) {
            organization.setActive(request.active());
        }
        Organization saved = organizationRepository.save(organization);
        return organizationMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationResponse getById(Long id) {
        return organizationMapper.toResponse(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrganizationResponse> list(Pageable pageable) {
        return organizationRepository.findAll(pageable).map(organizationMapper::toResponse);
    }

    private Organization getEntity(Long id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Organization", id));
    }
}
