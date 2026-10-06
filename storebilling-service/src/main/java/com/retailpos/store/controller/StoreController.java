package com.retailpos.store.controller;

import com.retailpos.auth.AccessValidator;
import com.retailpos.auth.userdetails.UserPrincipal;
import com.retailpos.common.dto.ApiResponse;
import com.retailpos.store.dto.CreateStoreRequest;
import com.retailpos.store.dto.StoreResponse;
import com.retailpos.store.dto.UpdateStoreRequest;
import com.retailpos.store.service.StoreService;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * No DELETE endpoint - soft delete only, via {@code PUT} with {@code active=false}.
 *
 * <p>{@code getById}/{@code update} are additionally checked against the caller's store access
 * via {@link AccessValidator} - the IDOR-prevention checkpoint required by security policy.
 */
@RestController
@RequestMapping("/api/v1/stores")
public class StoreController {

    private final StoreService storeService;
    private final AccessValidator accessValidator;

    public StoreController(StoreService storeService, AccessValidator accessValidator) {
        this.storeService = storeService;
        this.accessValidator = accessValidator;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('STORE_VIEW')")
    public ApiResponse<Page<StoreResponse>> list(Pageable pageable) {
        return ApiResponse.success(storeService.list(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('STORE_VIEW')")
    public ApiResponse<StoreResponse> getById(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        accessValidator.assertStoreAccess(principal, id);
        return ApiResponse.success(storeService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('STORE_CREATE')")
    public ApiResponse<StoreResponse> create(@Valid @RequestBody CreateStoreRequest request) {
        return ApiResponse.success("Store created.", storeService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('STORE_UPDATE')")
    public ApiResponse<StoreResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody UpdateStoreRequest request,
                                               @AuthenticationPrincipal UserPrincipal principal) {
        accessValidator.assertStoreAccess(principal, id);
        return ApiResponse.success("Store updated.", storeService.update(id, request));
    }
}
