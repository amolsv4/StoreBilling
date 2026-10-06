package com.retailpos.terminal.dto;

import com.retailpos.terminal.entity.TerminalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTerminalRequest(
        @NotNull Long storeId,
        @NotBlank @Size(max = 30) String code,
        @NotBlank @Size(max = 150) String name,
        @NotNull TerminalType terminalType,
        @Size(max = 100) String deviceIdentifier
) {
}
