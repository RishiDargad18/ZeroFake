package com.zerofake.fraud.exception;

import com.zerofake.fraud.dto.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final String GENERIC_ERROR_MESSAGE =
            "An unexpected error occurred. Please try again later.";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<ApiError>> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<ApiError>> handleBadRequestException(
            BadRequestException ex,
            HttpServletRequest request
    ) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request, null);
    }

    /**
     * A service this one depends on could not answer.
     *
     * <p>Reported as 502 so the caller can tell "we could not check" apart from
     * "we checked and it is fake" — conflating the two would be the worst
     * possible failure mode for an anti-counterfeiting system.
     */
    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ApiResponse<ApiError>> handleExternalServiceException(
            ExternalServiceException ex,
            HttpServletRequest request
    ) {

        log.error("Downstream service failure while processing {}", request.getRequestURI(), ex);

        return buildResponse(HttpStatus.BAD_GATEWAY, ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<ApiError>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid value for parameter '" + ex.getName() + "'.",
                request,
                null
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ApiError>> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        Map<String, String> validationErrors = new LinkedHashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(fieldError ->
                        validationErrors.putIfAbsent(
                                fieldError.getField(),
                                fieldError.getDefaultMessage()
                        ));

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed.",
                request,
                validationErrors
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ApiError>> handleException(
            Exception ex,
            HttpServletRequest request
    ) {

        log.error("Unhandled exception while processing {}", request.getRequestURI(), ex);

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                GENERIC_ERROR_MESSAGE,
                request,
                null
        );
    }

    private ResponseEntity<ApiResponse<ApiError>> buildResponse(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Map<String, String> validationErrors
    ) {

        LocalDateTime timestamp = LocalDateTime.now();

        ApiError apiError = ApiError.builder()
                .timestamp(timestamp)
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .validationErrors(validationErrors)
                .build();

        ApiResponse<ApiError> response = ApiResponse.<ApiError>builder()
                .timestamp(timestamp)
                .status(status.value())
                .success(false)
                .message(message)
                .data(apiError)
                .build();

        return ResponseEntity.status(status).body(response);
    }
}
