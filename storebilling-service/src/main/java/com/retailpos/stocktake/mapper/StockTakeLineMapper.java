package com.retailpos.stocktake.mapper;

import com.retailpos.stocktake.dto.StockTakeLineResponse;
import com.retailpos.stocktake.entity.StockTakeLine;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StockTakeLineMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productSku", source = "product.sku")
    @Mapping(target = "productName", source = "product.name")
    StockTakeLineResponse toResponse(StockTakeLine entity);
}
