package com.retailpos.auth.jwt;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Envelope-shaped 403 for authorization failures raised directly by the security filter chain
 * (as opposed to {@code @PreAuthorize} denials inside a controller method, which are instead
 * caught by {@code GlobalExceptionHandler} since they occur during normal dispatch).
 *
 * <p>Like {@link JwtAuthenticationEntryPoint}, this writes a fixed, fully-static JSON body
 * directly rather than depending on an autowired Jackson mapper bean (see that class's Javadoc
 * for why).
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                        AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("""
                {"success":false,"message":"You do not have permission to perform this action.","errorCode":"ACCESS_DENIED"}""");
    }
}
