package com.retailpos.payment.mapper;

import com.retailpos.payment.dto.PaymentResponse;
import com.retailpos.payment.entity.PaymentTransaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentMapper {

    @Mapping(target = "saleId", source = "sale.id")
    PaymentResponse toResponse(PaymentTransaction entity);
}
