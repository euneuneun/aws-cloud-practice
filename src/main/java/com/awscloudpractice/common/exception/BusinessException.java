package com.awscloudpractice.common.exception;

public class BusinessException extends RuntimeException {

    // 속성
    private final ErrorCode errorCode;

    // 생성자
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    // 기능
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
