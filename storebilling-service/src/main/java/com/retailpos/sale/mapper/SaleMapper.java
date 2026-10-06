package com.retailpos.sale.mapper;

import com.retailpos.sale.dto.SaleLineResponse;
import com.retailpos.sale.dto.SaleResponse;
import com.retailpos.sale.entity.Sale;
import com.retailpos.sale.entity.SaleLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

/**
 * "lines" is always excluded from the direct {@code Sale -> SaleResponse} mapping - it is
 * assembled by the service (from the freshly-loaded {@code sale_line} rows), the same pattern
 * {@code TaxGroupMapper}/{@code TaxGroupServiceImpl.toResponseWithComponents} uses for its
 * "components"/"totalPercentage" fields.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SaleMapper {

    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "storeId", source = "store.id")
    @Mapping(target = "storeCode", source = "store.code")
    @Mapping(target = "terminalId", source = "terminal.id")
    @Mapping(target = "terminalCode", source = "terminal.code")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "soldByUserId", source = "soldByUser.id")
    @Mapping(target = "soldByUsername", source = "soldByUser.username")
    @Mapping(target = "lines", ignore = true)
    SaleResponse toResponse(Sale entity);

    @Mapping(target = "productId", source = "product.id")
    SaleLineResponse toLineResponse(SaleLine entity);
}
