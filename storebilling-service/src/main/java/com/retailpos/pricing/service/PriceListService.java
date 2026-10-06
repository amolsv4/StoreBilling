package com.retailpos.pricing.service;

import com.retailpos.pricing.dto.CreatePriceListRequest;
import com.retailpos.pricing.dto.PriceListResponse;
import com.retailpos.pricing.dto.UpdatePriceListRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PriceListService {

    PriceListResponse create(CreatePriceListRequest request);

    PriceListResponse update(Long id, UpdatePriceListRequest request);

    PriceListResponse getById(Long id);

    Page<PriceListResponse> list(Long organizationId, Pageable pageable);
}
