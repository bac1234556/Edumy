package com.edumy.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "Yêu cầu không hợp lệ."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Chưa xác thực hoặc phiên đăng nhập đã hết hạn."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "Bạn không có quyền truy cập tài nguyên này."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "Không tìm thấy tài nguyên yêu cầu."),
    CONFLICT(HttpStatus.CONFLICT, "Dữ liệu bị trùng lặp hoặc xung đột."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Đã xảy ra lỗi hệ thống nội bộ.");

    private final HttpStatus status;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.status = status;
        this.defaultMessage = defaultMessage;
    }
}
