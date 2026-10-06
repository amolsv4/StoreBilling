package com.retailpos.configuration.mapper;

import com.retailpos.configuration.dto.ConfigurationResponse;
import com.retailpos.configuration.dto.CreateConfigurationRequest;
import com.retailpos.configuration.dto.UpdateConfigurationRequest;
import com.retailpos.configuration.entity.AppConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ConfigurationMapper {

    AppConfiguration toEntity(CreateConfigurationRequest request);

    ConfigurationResponse toResponse(AppConfiguration entity);

    void updateEntityFromRequest(UpdateConfigurationRequest request, @MappingTarget AppConfiguration entity);
}
