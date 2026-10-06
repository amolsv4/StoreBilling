package com.retailpos.stocktransfer.mapper;

import com.retailpos.stocktransfer.dto.StockTransferLineResponse;
import com.retailpos.stocktransfer.entity.StockTransferLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StockTransferLineMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productSku", source = "product.sku")
    @Mapping(target = "productName", source = "product.name")
    StockTransferLineResponse toResponse(StockTransferLine entity);
}
