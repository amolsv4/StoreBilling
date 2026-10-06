package com.retailpos.auth.service;

import com.retailpos.auth.dto.LoginRequest;
import com.retailpos.auth.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request, String ipAddress);

    void logout(String username, String ipAddress);
}
