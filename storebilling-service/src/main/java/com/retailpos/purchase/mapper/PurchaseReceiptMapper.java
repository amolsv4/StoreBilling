package com.retailpos.purchase.mapper;

import com.retailpos.purchase.dto.CreatePurchaseReceiptRequest;
import com.retailpos.purchase.dto.UpdatePurchaseReceiptRequest;
import com.retailpos.purchase.entity.PurchaseReceipt;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * Only handles the header's own scalar fields - {@code lines}/{@code totalAmount} are always
 * server-computed (see {@code PurchaseReceiptServiceImpl}), so {@code PurchaseReceiptResponse} is
 * built manually in the service rather than through this mapper.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PurchaseReceiptMapper {

    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "store", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    PurchaseReceipt toEntity(CreatePurchaseReceiptRequest request);

    void updateEntityFromRequest(UpdatePurchaseReceiptRequest request, @MappingTarget PurchaseReceipt entity);
}
