package com.retailpos.tax.service;

import com.retailpos.tax.dto.CreateTaxGroupRequest;
import com.retailpos.tax.dto.TaxGroupResponse;
import com.retailpos.tax.dto.UpdateTaxGroupRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface TaxGroupService {

    TaxGroupResponse create(CreateTaxGroupRequest request);

    TaxGroupResponse update(Long id, UpdateTaxGroupRequest request);

    TaxGroupResponse getById(Long id);

    Page<TaxGroupResponse> list(Long organizationId, Pageable pageable);

    /**
     * Sum of the currently-active tax components linked to this group - the same rule used to
     * compute {@code TaxGroupResponse.totalPercentage}, exposed here so Sales can snapshot a sale
     * line's {@code taxPercentage} at add-time without duplicating the summing logic.
     */
    BigDecimal getEffectiveTotalPercentage(Long taxGroupId);
}
