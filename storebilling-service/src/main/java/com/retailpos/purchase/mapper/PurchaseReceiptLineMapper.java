package com.retailpos.purchase.mapper;

import com.retailpos.purchase.dto.PurchaseReceiptLineResponse;
import com.retailpos.purchase.entity.PurchaseReceiptLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PurchaseReceiptLineMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productSku", source = "product.sku")
    @Mapping(target = "productName", source = "product.name")
    PurchaseReceiptLineResponse toResponse(PurchaseReceiptLine entity);
}
