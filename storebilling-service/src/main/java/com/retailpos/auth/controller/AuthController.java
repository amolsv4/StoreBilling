package com.retailpos.auth.controller;

import com.retailpos.auth.dto.LoginRequest;
import com.retailpos.auth.dto.LoginResponse;
import com.retailpos.auth.service.AuthService;
import com.retailpos.auth.userdetails.UserPrincipal;
import com.retailpos.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Public endpoints (see SecurityConfig: {@code /api/v1/auth/**} is permitAll - login obviously
 * must be reachable without a token, and logout is a best-effort audit-log write for this phase).
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        LoginResponse response = authService.login(request, resolveClientIp(httpRequest));
        return ApiResponse.success("Login successful.", response);
    }

    /**
     * No server-side token blacklist in Phase 1 (see AuthServiceImpl) - this just records a
     * LOGOUT audit entry. If a valid bearer token was presented, JwtAuthenticationFilter will
     * already have populated the principal even though this endpoint itself is public.
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal UserPrincipal principal, HttpServletRequest httpRequest) {
        String username = principal != null ? principal.getUsername() : "UNKNOWN";
        authService.logout(username, resolveClientIp(httpRequest));
        return ApiResponse.success("Logout recorded.", null);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
