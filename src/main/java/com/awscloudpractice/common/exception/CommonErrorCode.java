package com.awscloudpractice.common.exception;

import org.springframework.http.HttpStatus;

public enum CommonErrorCode implements ErrorCode {

    // 400 Bad Request: 검증 실패, 본문 파싱 실패, 파라미터 타입 오류, 파트 누락, 잘못된 multipart 요청
    INVALID_INPUT_VALUE(
            "COMMON_INVALID_INPUT_VALUE", "Invalid input value.", HttpStatus.BAD_REQUEST),

    // 404 Not Found
    RESOURCE_NOT_FOUND("COMMON_RESOURCE_NOT_FOUND", "Resource not found.", HttpStatus.NOT_FOUND),

    // 413 Content Too Large
    FILE_SIZE_EXCEEDED(
            "COMMON_FILE_SIZE_EXCEEDED",
            "File size exceeds the limit.",
            HttpStatus.CONTENT_TOO_LARGE),

    // 500 Internal Server Error
    INTERNAL_SERVER_ERROR(
            "COMMON_INTERNAL_SERVER_ERROR",
            "Internal Server Error",
            HttpStatus.INTERNAL_SERVER_ERROR);

    // 속성
    private final String code;
    private final String message;
    private final HttpStatus statusCode;

    // 생성자
    CommonErrorCode(String code, String message, HttpStatus statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    // 기능
    @Override
    public HttpStatus getStatusCode() {
        return statusCode;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getCode() {
        return code;
    }
}
