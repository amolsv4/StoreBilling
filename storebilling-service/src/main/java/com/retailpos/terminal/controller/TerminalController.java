package com.retailpos.terminal.controller;

import com.retailpos.auth.AccessValidator;
import com.retailpos.auth.userdetails.UserPrincipal;
import com.retailpos.common.dto.ApiResponse;
import com.retailpos.terminal.dto.CreateTerminalRequest;
import com.retailpos.terminal.dto.TerminalResponse;
import com.retailpos.terminal.dto.UpdateTerminalRequest;
import com.retailpos.terminal.service.TerminalService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * No DELETE endpoint - soft delete only, via {@code PUT} with {@code active=false}.
 *
 * <p>{@code create}/{@code getById}/{@code update} are checked against the caller's store access
 * via {@link AccessValidator} (a terminal's store is resolved internally for the access check).
 */
@RestController
@RequestMapping("/api/v1/terminals")
public class TerminalController {

    private final TerminalService terminalService;
    private final AccessValidator accessValidator;

    public TerminalController(TerminalService terminalService, AccessValidator accessValidator) {
        this.terminalService = terminalService;
        this.accessValidator = accessValidator;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('TERMINAL_VIEW')")
    public ApiResponse<Page<TerminalResponse>> list(@RequestParam(required = false) Long storeId, Pageable pageable) {
        if (storeId != null) {
            return ApiResponse.success(terminalService.listByStore(storeId, pageable));
        }
        return ApiResponse.success(terminalService.list(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TERMINAL_VIEW')")
    public ApiResponse<TerminalResponse> getById(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        accessValidator.assertTerminalAccess(principal, id);
        return ApiResponse.success(terminalService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('TERMINAL_CREATE')")
    public ApiResponse<TerminalResponse> create(@Valid @RequestBody CreateTerminalRequest request,
                                                  @AuthenticationPrincipal UserPrincipal principal) {
        accessValidator.assertStoreAccess(principal, request.storeId());
        return ApiResponse.success("Terminal created.", terminalService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('TERMINAL_UPDATE')")
    public ApiResponse<TerminalResponse> update(@PathVariable Long id,
                                                  @Valid @RequestBody UpdateTerminalRequest request,
                                                  @AuthenticationPrincipal UserPrincipal principal) {
        accessValidator.assertTerminalAccess(principal, id);
        return ApiResponse.success("Terminal updated.", terminalService.update(id, request));
    }
}
