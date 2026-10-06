package com.retailpos.auth.service;

import com.retailpos.audit.service.AuditService;
import com.retailpos.auth.dto.LoginRequest;
import com.retailpos.auth.dto.LoginResponse;
import com.retailpos.auth.jwt.JwtTokenProvider;
import com.retailpos.auth.userdetails.UserPrincipal;
import com.retailpos.user.entity.User;
import com.retailpos.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuditService auditService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(authenticationManager, jwtTokenProvider, userRepository, auditService);
    }

    @Test
    void loginSucceedsAndIssuesTokenAndAuditsSuccess() {
        UserPrincipal principal = new UserPrincipal(1L, "jdoe", "hash", true, false,
                5L, true, Set.of(), Set.of(), List.of());
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, Set.of());
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtTokenProvider.generateToken(principal)).thenReturn("signed.jwt.token");
        when(jwtTokenProvider.getExpirationMs()).thenReturn(1_800_000L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(new User()));

        LoginResponse response = authService.login(new LoginRequest("jdoe", "correct-password"), "127.0.0.1");

        assertThat(response.accessToken()).isEqualTo("signed.jwt.token");
        assertThat(response.username()).isEqualTo("jdoe");
        assertThat(response.userId()).isEqualTo(1L);
        verify(auditService).recordLoginSuccess("jdoe", "127.0.0.1");
        verify(auditService, never()).recordLoginFailure(anyString(), anyString(), anyString());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void loginWithBadCredentialsAuditsFailureAndRethrows() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatExceptionOfType(BadCredentialsException.class)
                .isThrownBy(() -> authService.login(new LoginRequest("jdoe", "wrong-password"), "127.0.0.1"));

        verify(auditService).recordLoginFailure(eq("jdoe"), eq("127.0.0.1"), anyString());
        verify(jwtTokenProvider, never()).generateToken(any());
    }

    @Test
    void loginWithLockedAccountAuditsFailureAndRethrows() {
        when(authenticationManager.authenticate(any())).thenThrow(new LockedException("Account is locked"));

        assertThatExceptionOfType(LockedException.class)
                .isThrownBy(() -> authService.login(new LoginRequest("jdoe", "any-password"), "10.0.0.5"));

        verify(auditService).recordLoginFailure(eq("jdoe"), eq("10.0.0.5"), anyString());
    }

    @Test
    void loginWithDisabledAccountAuditsFailureAndRethrows() {
        when(authenticationManager.authenticate(any())).thenThrow(new DisabledException("Account is disabled"));

        assertThatExceptionOfType(DisabledException.class)
                .isThrownBy(() -> authService.login(new LoginRequest("jdoe", "any-password"), "10.0.0.5"));

        verify(auditService).recordLoginFailure(eq("jdoe"), eq("10.0.0.5"), anyString());
    }

    @Test
    void logoutRecordsAuditEntry() {
        authService.logout("jdoe", "127.0.0.1");

        verify(auditService).recordLogout("jdoe", "127.0.0.1");
    }
}
