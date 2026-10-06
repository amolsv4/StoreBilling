package com.retailpos.common.exception;

import com.retailpos.common.dto.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void resourceNotFoundMapsTo404WithEnvelope() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleResourceNotFound(ResourceNotFoundException.forEntity("Store", 99L));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getErrorCode()).isEqualTo("RESOURCE_NOT_FOUND");
        assertThat(response.getBody().getMessage()).contains("Store").contains("99");
    }

    @Test
    void duplicateResourceMapsTo409WithEnvelope() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleDuplicateResource(new DuplicateResourceException("Code already exists"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getErrorCode()).isEqualTo("DUPLICATE_RESOURCE");
    }

    @Test
    void businessExceptionMapsTo400WithEnvelope() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleBusinessException(new BusinessException("Not allowed", "CUSTOM_CODE"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().getErrorCode()).isEqualTo("CUSTOM_CODE");
    }

    @Test
    void authenticationExceptionMapsTo401WithEnvelope() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleAuthenticationException(new BadCredentialsException("Bad credentials"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getErrorCode()).isEqualTo("AUTHENTICATION_FAILED");
    }

    @Test
    void accessDeniedMapsTo403WithEnvelope() {
        ResponseEntity<ApiResponse<Void>> response =
                handler.handleAccessDenied(new AccessDeniedException("Denied"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody().getErrorCode()).isEqualTo("ACCESS_DENIED");
    }

    @Test
    void unexpectedExceptionMapsTo500WithEnvelope() {
        ResponseEntity<ApiResponse<Void>> response = handler.handleUnexpected(new RuntimeException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().getErrorCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().getMessage()).doesNotContain("boom");
    }
}
