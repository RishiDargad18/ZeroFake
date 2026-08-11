package com.zerofake.auth.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerofake.auth.dto.common.ApiResponse;
import com.zerofake.auth.exception.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Returns the standard {@link ApiResponse} error envelope when an unauthenticated
 * request reaches a protected endpoint, instead of Spring Security's default
 * empty response body.
 */
@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        LocalDateTime timestamp = LocalDateTime.now();

        ApiError error = ApiError.builder()
                .timestamp(timestamp)
                .status(HttpStatus.UNAUTHORIZED.value())
                .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                .message("Authentication is required to access this resource.")
                .path(request.getRequestURI())
                .build();

        ApiResponse<ApiError> body = ApiResponse.<ApiError>builder()
                .timestamp(timestamp)
                .status(HttpStatus.UNAUTHORIZED.value())
                .success(false)
                .message("Authentication is required to access this resource.")
                .data(error)
                .build();

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), body);
    }
}
