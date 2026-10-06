package com.retailpos.terminal.dto;

import com.retailpos.terminal.entity.TerminalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateTerminalRequest(
        @NotBlank @Size(max = 150) String name,
        @NotNull TerminalType terminalType,
        @Size(max = 100) String deviceIdentifier,
        Boolean active
) {
}
