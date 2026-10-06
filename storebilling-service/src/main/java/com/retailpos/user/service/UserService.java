package com.retailpos.user.service;

import com.retailpos.user.dto.CreateUserRequest;
import com.retailpos.user.dto.UpdateUserRequest;
import com.retailpos.user.dto.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse create(CreateUserRequest request);

    UserResponse update(Long id, UpdateUserRequest request);

    UserResponse getById(Long id);

    Page<UserResponse> list(Pageable pageable);
}
