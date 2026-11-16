package com.edumy.shared.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    private final HttpStatus status;
    private final String code;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.status = errorCode.getStatus();
        this.code = errorCode.name();
    }

    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.status = errorCode.getStatus();
        this.code = errorCode.name();
    }

    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.errorCode = ErrorCode.BAD_REQUEST;
        this.status = status;
        this.code = "BAD_REQUEST";
    }

    public BusinessException(HttpStatus status, String code, String message) {
        super(message);
        this.errorCode = ErrorCode.BAD_REQUEST;
        this.status = status;
        this.code = code;
    }
}
