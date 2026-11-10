package com.edumy.common.exception;

import com.edumy.common.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Global Exception Handler returning standardized ApiResponse envelopes.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(ApiResponse.error(ex.getMessage(), Map.of("code", ex.getErrorCode().name())));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFoundException(NoSuchElementException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : ErrorCode.NOT_FOUND.getDefaultMessage();
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(msg, Map.of("code", ErrorCode.NOT_FOUND.name())));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequestException(IllegalArgumentException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : ErrorCode.BAD_REQUEST.getDefaultMessage();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(msg, Map.of("code", ErrorCode.BAD_REQUEST.name())));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : ErrorCode.FORBIDDEN.getDefaultMessage();
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(msg, Map.of("code", ErrorCode.FORBIDDEN.name())));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Dữ liệu đầu vào không hợp lệ.", fieldErrors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : ErrorCode.INTERNAL_ERROR.getDefaultMessage();
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Đã xảy ra lỗi hệ thống: " + msg, Map.of("exception", ex.getClass().getSimpleName())));
    }
}
