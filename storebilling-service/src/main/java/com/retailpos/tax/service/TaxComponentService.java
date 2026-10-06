package com.retailpos.tax.service;

import com.retailpos.tax.dto.CreateTaxComponentRequest;
import com.retailpos.tax.dto.TaxComponentResponse;
import com.retailpos.tax.dto.UpdateTaxComponentRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TaxComponentService {

    TaxComponentResponse create(CreateTaxComponentRequest request);

    TaxComponentResponse update(Long id, UpdateTaxComponentRequest request);

    TaxComponentResponse getById(Long id);

    Page<TaxComponentResponse> list(Long organizationId, Pageable pageable);
}
