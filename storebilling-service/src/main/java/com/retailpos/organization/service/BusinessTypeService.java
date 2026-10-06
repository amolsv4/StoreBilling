package com.retailpos.organization.service;

import com.retailpos.organization.dto.BusinessTypeResponse;

import java.util.List;

/**
 * Read-only: business types are reference rows seeded via Flyway (V6), not user-managed CRUD, so
 * this Phase 1 service intentionally exposes no create/update operations.
 */
public interface BusinessTypeService {

    List<BusinessTypeResponse> listAll();

    BusinessTypeResponse getById(Long id);
}
