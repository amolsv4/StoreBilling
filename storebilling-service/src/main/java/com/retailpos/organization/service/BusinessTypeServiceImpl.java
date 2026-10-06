package com.retailpos.organization.service;

import com.retailpos.common.exception.ResourceNotFoundException;
import com.retailpos.organization.dto.BusinessTypeResponse;
import com.retailpos.organization.mapper.BusinessTypeMapper;
import com.retailpos.organization.repository.BusinessTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BusinessTypeServiceImpl implements BusinessTypeService {

    private final BusinessTypeRepository businessTypeRepository;
    private final BusinessTypeMapper businessTypeMapper;

    public BusinessTypeServiceImpl(BusinessTypeRepository businessTypeRepository, BusinessTypeMapper businessTypeMapper) {
        this.businessTypeRepository = businessTypeRepository;
        this.businessTypeMapper = businessTypeMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BusinessTypeResponse> listAll() {
        return businessTypeRepository.findAll().stream().map(businessTypeMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BusinessTypeResponse getById(Long id) {
        return businessTypeRepository.findById(id)
                .map(businessTypeMapper::toResponse)
                .orElseThrow(() -> ResourceNotFoundException.forEntity("BusinessType", id));
    }
}
