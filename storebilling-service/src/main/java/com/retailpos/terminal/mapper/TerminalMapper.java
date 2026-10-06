package com.retailpos.terminal.mapper;

import com.retailpos.terminal.dto.CreateTerminalRequest;
import com.retailpos.terminal.dto.TerminalResponse;
import com.retailpos.terminal.dto.UpdateTerminalRequest;
import com.retailpos.terminal.entity.Terminal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TerminalMapper {

    @Mapping(target = "store", ignore = true)
    Terminal toEntity(CreateTerminalRequest request);

    @Mapping(target = "storeId", source = "store.id")
    @Mapping(target = "storeCode", source = "store.code")
    TerminalResponse toResponse(Terminal entity);

    /** "active" is intentionally excluded - the service sets it explicitly when non-null. */
    @Mapping(target = "active", ignore = true)
    void updateEntityFromRequest(UpdateTerminalRequest request, @MappingTarget Terminal entity);
}
