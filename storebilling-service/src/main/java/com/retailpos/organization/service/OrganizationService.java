package com.retailpos.organization.service;

import com.retailpos.organization.dto.CreateOrganizationRequest;
import com.retailpos.organization.dto.OrganizationResponse;
import com.retailpos.organization.dto.UpdateOrganizationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrganizationService {

    OrganizationResponse create(CreateOrganizationRequest request);

    OrganizationResponse update(Long id, UpdateOrganizationRequest request);

    OrganizationResponse getById(Long id);

    Page<OrganizationResponse> list(Pageable pageable);
}
