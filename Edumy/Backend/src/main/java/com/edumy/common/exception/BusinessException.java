package com.edumy.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    private final HttpStatus status;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.status = errorCode.getStatus();
    }

    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.status = errorCode.getStatus();
    }

    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.errorCode = ErrorCode.BAD_REQUEST;
        this.status = status;
    }
}
