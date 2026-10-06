package com.retailpos.auth.service;

import com.retailpos.audit.service.AuditService;
import com.retailpos.auth.dto.LoginRequest;
import com.retailpos.auth.dto.LoginResponse;
import com.retailpos.auth.jwt.JwtTokenProvider;
import com.retailpos.auth.userdetails.UserPrincipal;
import com.retailpos.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Phase 1 simplification: {@link #logout} has no server-side token blacklist - JWTs are
 * stateless and simply expire on their own; logout only writes an audit-log LOGOUT entry so the
 * event is traceable. A real revocation mechanism (e.g. a denylist keyed by {@code jti}, or
 * short-lived tokens plus refresh tokens) is left to a later phase.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider,
                            UserRepository userRepository, AuditService auditService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        } catch (AuthenticationException ex) {
            auditService.recordLoginFailure(request.username(), ipAddress,
                    ex.getClass().getSimpleName() + ": " + ex.getMessage());
            throw ex;
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        SecurityContextHolder.getContext().setAuthentication(authentication);

        userRepository.findById(principal.getId()).ifPresent(user -> {
            user.setLastLoginDate(LocalDateTime.now());
            userRepository.save(user);
        });

        String token = jwtTokenProvider.generateToken(principal);
        auditService.recordLoginSuccess(principal.getUsername(), ipAddress);

        return new LoginResponse(
                token,
                "Bearer",
                jwtTokenProvider.getExpirationMs(),
                principal.getId(),
                principal.getUsername(),
                principal.getRoleCodes(),
                principal.getOrganizationId(),
                principal.isAllStoreAccess(),
                List.copyOf(principal.getStoreIds()));
    }

    @Override
    public void logout(String username, String ipAddress) {
        auditService.recordLogout(username, ipAddress);
    }
}
