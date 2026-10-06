package com.retailpos.stocktransfer.mapper;

import com.retailpos.stocktransfer.dto.CreateStockTransferRequest;
import com.retailpos.stocktransfer.dto.UpdateStockTransferRequest;
import com.retailpos.stocktransfer.entity.StockTransfer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

/**
 * Only handles the header's own scalar fields - {@code organization}/{@code fromStore}/
 * {@code toStore}/{@code status}/{@code lines} are always resolved/set explicitly in
 * {@code StockTransferServiceImpl}.
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StockTransferMapper {

    @Mapping(target = "organization", ignore = true)
    @Mapping(target = "fromStore", ignore = true)
    @Mapping(target = "toStore", ignore = true)
    @Mapping(target = "status", ignore = true)
    StockTransfer toEntity(CreateStockTransferRequest request);

    @Mapping(target = "fromStore", ignore = true)
    @Mapping(target = "toStore", ignore = true)
    void updateEntityFromRequest(UpdateStockTransferRequest request, @MappingTarget StockTransfer entity);
}
