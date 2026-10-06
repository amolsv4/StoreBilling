package com.retailpos.stocktake.mapper;

import com.retailpos.stocktake.dto.CreateStockTakeRequest;
import com.retailpos.stocktake.dto.UpdateStockTakeRequest;
import com.retailpos.stocktake.entity.StockTake;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * Only handles the header's own scalar fields - {@code organization}/{@code store}/
 * {@code status}/{@code lines} are always resolved/set explicitly in {@code StockTakeServiceImpl}.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StockTakeMapper {

    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "store", ignore = true)
    @Mapping(target = "status", ignore = true)
    StockTake toEntity(CreateStockTakeRequest request);

    void updateEntityFromRequest(UpdateStockTakeRequest request, @MappingTarget StockTake entity);
}
