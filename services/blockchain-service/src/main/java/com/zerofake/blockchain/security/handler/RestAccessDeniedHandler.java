package com.zerofake.blockchain.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerofake.blockchain.dto.common.ApiResponse;
import com.zerofake.blockchain.exception.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Returns the standard ApiResponse error envelope when an authenticated user
 * lacks the role required by an endpoint.
 */
@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {

    private static final String MESSAGE =
            "You do not have permission to perform this action.";

    private final ObjectMapper objectMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        LocalDateTime timestamp = LocalDateTime.now();

        ApiError error = ApiError.builder()
                .timestamp(timestamp)
                .status(HttpStatus.FORBIDDEN.value())
                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                .message(MESSAGE)
                .path(request.getRequestURI())
                .build();

        ApiResponse<ApiError> body = ApiResponse.<ApiError>builder()
                .timestamp(timestamp)
                .status(HttpStatus.FORBIDDEN.value())
                .success(false)
                .message(MESSAGE)
                .data(error)
                .build();

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), body);
    }
}
