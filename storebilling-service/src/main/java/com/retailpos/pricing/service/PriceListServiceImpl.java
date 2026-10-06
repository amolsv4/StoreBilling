package com.retailpos.pricing.service;

import com.retailpos.common.exception.DuplicateResourceException;
import com.retailpos.common.exception.ResourceNotFoundException;
import com.retailpos.organization.entity.Organization;
import com.retailpos.organization.repository.OrganizationRepository;
import com.retailpos.pricing.dto.CreatePriceListRequest;
import com.retailpos.pricing.dto.PriceListResponse;
import com.retailpos.pricing.dto.UpdatePriceListRequest;
import com.retailpos.pricing.entity.PriceList;
import com.retailpos.pricing.mapper.PriceListMapper;
import com.retailpos.pricing.repository.PriceListRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PriceListServiceImpl implements PriceListService {

    private final PriceListRepository priceListRepository;
    private final OrganizationRepository organizationRepository;
    private final PriceListMapper priceListMapper;

    public PriceListServiceImpl(PriceListRepository priceListRepository,
                                 OrganizationRepository organizationRepository,
                                 PriceListMapper priceListMapper) {
        this.priceListRepository = priceListRepository;
        this.organizationRepository = organizationRepository;
        this.priceListMapper = priceListMapper;
    }

    @Override
    @Transactional
    public PriceListResponse create(CreatePriceListRequest request) {
        if (priceListRepository.existsByOrganizationIdAndCode(request.organizationId(), request.code())) {
            throw new DuplicateResourceException(
                    "A price list with code '" + request.code() + "' already exists for this organization.");
        }
        Organization organization = organizationRepository.findById(request.organizationId())
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Organization", request.organizationId()));

        PriceList priceList = priceListMapper.toEntity(request);
        priceList.setOrganization(organization);
        PriceList saved = priceListRepository.save(priceList);
        return priceListMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public PriceListResponse update(Long id, UpdatePriceListRequest request) {
        PriceList priceList = getEntity(id);
        priceListMapper.updateEntityFromRequest(request, priceList);
        if (request.active() != null) {
            priceList.setActive(request.active());
        }
        PriceList saved = priceListRepository.save(priceList);
        return priceListMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PriceListResponse getById(Long id) {
        return priceListMapper.toResponse(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PriceListResponse> list(Long organizationId, Pageable pageable) {
        if (organizationId != null) {
            return priceListRepository.findAllByOrganizationId(organizationId, pageable).map(priceListMapper::toResponse);
        }
        return priceListRepository.findAll(pageable).map(priceListMapper::toResponse);
    }

    private PriceList getEntity(Long id) {
        return priceListRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("PriceList", id));
    }
}
