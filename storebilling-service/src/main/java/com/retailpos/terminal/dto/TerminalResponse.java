package com.retailpos.terminal.dto;

import com.retailpos.terminal.entity.TerminalType;

import java.time.LocalDateTime;

public record TerminalResponse(
        Long id,
        Long storeId,
        String storeCode,
        String code,
        String name,
        TerminalType terminalType,
        String deviceIdentifier,
        boolean active,
        LocalDateTime createdDate,
        LocalDateTime updatedDate
) {
}
