package com.retailpos.auth.jwt;

import com.retailpos.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Writes the standard {@link ApiResponse} envelope (instead of Spring Security's default
 * WWW-Authenticate/empty-body response) whenever an unauthenticated request hits a protected
 * endpoint - covers both "no token" and "token rejected by JwtAuthenticationFilter" cases, since
 * either way the SecurityContext ends up unauthenticated by the time authorization runs.
 *
 * <p>This fires from the security filter chain, outside normal Spring MVC dispatch, so it cannot
 * rely on an autowired Jackson {@code ObjectMapper}/{@code JsonMapper} bean (Spring Boot 4 no
 * longer auto-configures a classic {@code com.fasterxml.jackson.databind.ObjectMapper} bean by
 * default). The response body is a fixed, fully-static shape with no user-controlled content, so
 * it is safe to write as a literal JSON string rather than serializing via a mapper.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("""
                {"success":false,"message":"Authentication is required to access this resource.","errorCode":"AUTHENTICATION_REQUIRED"}""");
    }
}
