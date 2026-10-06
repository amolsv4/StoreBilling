package com.retailpos.store.service;

import com.retailpos.common.exception.BusinessException;
import com.retailpos.common.exception.DuplicateResourceException;
import com.retailpos.common.exception.ResourceNotFoundException;
import com.retailpos.organization.entity.BusinessType;
import com.retailpos.organization.entity.Organization;
import com.retailpos.organization.repository.BusinessTypeRepository;
import com.retailpos.organization.repository.OrganizationRepository;
import com.retailpos.pricing.entity.PriceList;
import com.retailpos.pricing.repository.PriceListRepository;
import com.retailpos.store.dto.CreateStoreRequest;
import com.retailpos.store.dto.StoreResponse;
import com.retailpos.store.dto.UpdateStoreRequest;
import com.retailpos.store.entity.Store;
import com.retailpos.store.mapper.StoreMapper;
import com.retailpos.store.repository.StoreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StoreServiceImpl implements StoreService {

    private final StoreRepository storeRepository;
    private final OrganizationRepository organizationRepository;
    private final BusinessTypeRepository businessTypeRepository;
    private final PriceListRepository priceListRepository;
    private final StoreMapper storeMapper;

    public StoreServiceImpl(StoreRepository storeRepository,
                             OrganizationRepository organizationRepository,
                             BusinessTypeRepository businessTypeRepository,
                             PriceListRepository priceListRepository,
                             StoreMapper storeMapper) {
        this.storeRepository = storeRepository;
        this.organizationRepository = organizationRepository;
        this.businessTypeRepository = businessTypeRepository;
        this.priceListRepository = priceListRepository;
        this.storeMapper = storeMapper;
    }

    @Override
    @Transactional
    public StoreResponse create(CreateStoreRequest request) {
        if (storeRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("A store with code '" + request.code() + "' already exists.");
        }
        Organization organization = organizationRepository.findById(request.organizationId())
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Organization", request.organizationId()));
        BusinessType businessType = businessTypeRepository.findById(request.businessTypeId())
                .orElseThrow(() -> ResourceNotFoundException.forEntity("BusinessType", request.businessTypeId()));

        Store store = storeMapper.toEntity(request);
        store.setOrganization(organization);
        store.setBusinessType(businessType);
        if (store.getTimezone() == null || store.getTimezone().isBlank()) {
            store.setTimezone("UTC");
        }
        Store saved = storeRepository.save(store);
        return storeMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public StoreResponse update(Long id, UpdateStoreRequest request) {
        Store store = getEntity(id);
        storeMapper.updateEntityFromRequest(request, store);
        if (request.active() != null) {
            store.setActive(request.active());
        }
        if (request.defaultPriceListId() != null) {
            PriceList priceList = priceListRepository.findById(request.defaultPriceListId())
                    .orElseThrow(() -> ResourceNotFoundException.forEntity("PriceList", request.defaultPriceListId()));
            if (!priceList.getOrganization().getId().equals(store.getOrganization().getId())) {
                throw new BusinessException(
                        "Price list '" + priceList.getCode() + "' belongs to a different organization.");
            }
            store.setDefaultPriceListId(priceList.getId());
        }
        Store saved = storeRepository.save(store);
        return storeMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public StoreResponse getById(Long id) {
        return storeMapper.toResponse(getEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<StoreResponse> list(Pageable pageable) {
        return storeRepository.findAll(pageable).map(storeMapper::toResponse);
    }

    private Store getEntity(Long id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("Store", id));
    }
}
