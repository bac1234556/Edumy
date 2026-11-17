package com.edumy.shared.exception;

import com.edumy.shared.response.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Global Exception Handler returning standardized ApiResponse envelopes.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {
        String code = ex.getCode() != null ? ex.getCode() : (ex.getErrorCode() != null ? ex.getErrorCode().name() : "ERROR");
        String traceId = resolveTraceId();
        return ResponseEntity
                .status(ex.getStatus())
                .body(ApiResponse.error(ex.getMessage(), buildErrors(code, null, traceId)));
    }

    @ExceptionHandler({NoSuchElementException.class, NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ApiResponse<Void>> handleNotFoundException(Exception ex) {
        String traceId = resolveTraceId();
        String msg = ex instanceof NoSuchElementException && ex.getMessage() != null
                ? ex.getMessage()
                : ErrorCode.NOT_FOUND.getDefaultMessage();
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(msg, buildErrors(ErrorCode.NOT_FOUND.name(), null, traceId)));
    }

    @ExceptionHandler({
            IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequestException(Exception ex) {
        String traceId = resolveTraceId();
        String msg = ex instanceof IllegalArgumentException && ex.getMessage() != null
                ? ex.getMessage()
                : ErrorCode.BAD_REQUEST.getDefaultMessage();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(msg, buildErrors(ErrorCode.BAD_REQUEST.name(), null, traceId)));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        String traceId = resolveTraceId();
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error(ErrorCode.METHOD_NOT_ALLOWED.getDefaultMessage(),
                        buildErrors(ErrorCode.METHOD_NOT_ALLOWED.name(), null, traceId)));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        String traceId = resolveTraceId();
        String msg = ex.getMessage() != null ? ex.getMessage() : ErrorCode.FORBIDDEN.getDefaultMessage();
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(msg, buildErrors(ErrorCode.FORBIDDEN.name(), null, traceId)));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String traceId = resolveTraceId();
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(ErrorCode.VALIDATION_ERROR.getDefaultMessage(),
                        buildErrors(ErrorCode.VALIDATION_ERROR.name(), fieldErrors, traceId)));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception ex) {
        String traceId = resolveTraceId();
        log.error("Unhandled exception [traceId={}]", traceId, ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ErrorCode.INTERNAL_ERROR.getDefaultMessage(),
                        buildErrors(ErrorCode.INTERNAL_ERROR.name(), null, traceId)));
    }

    private String resolveTraceId() {
        String traceId = MDC.get("traceId");
        return (traceId != null && !traceId.isBlank()) ? traceId : UUID.randomUUID().toString();
    }

    private Map<String, Object> buildErrors(String code, Map<String, String> fields, String traceId) {
        Map<String, Object> errors = new LinkedHashMap<>();
        errors.put("code", code);
        errors.put("fields", fields);
        errors.put("traceId", traceId);
        return errors;
    }
}
