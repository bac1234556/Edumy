package com.edumy.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standardized Response Envelope for Edumy Modular Monolith Backend.
 * Contract:
 * {
 *   "success": true,
 *   "message": "...",
 *   "data": { ... },
 *   "errors": null
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private Object errors;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("OK")
                .data(data)
                .errors(null)
                .build();
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message != null ? message : "OK")
                .data(data)
                .errors(null)
                .build();
    }

    public static <T> ApiResponse<T> error(String message, Object errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .errors(errors)
                .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .data(null)
                .errors(null)
                .build();
    }
}
