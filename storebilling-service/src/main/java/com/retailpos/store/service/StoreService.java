package com.retailpos.store.service;

import com.retailpos.store.dto.CreateStoreRequest;
import com.retailpos.store.dto.StoreResponse;
import com.retailpos.store.dto.UpdateStoreRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface StoreService {

    StoreResponse create(CreateStoreRequest request);

    StoreResponse update(Long id, UpdateStoreRequest request);

    StoreResponse getById(Long id);

    Page<StoreResponse> list(Pageable pageable);
}
