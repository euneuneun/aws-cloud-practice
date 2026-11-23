package com.awscloudpractice.common.response;

import java.util.Map;

public class ErrorResponse {

    // 속성
    private String errorCode;

    private String message;

    private Map<String, String> fieldErrors;

    // 생성자
    public ErrorResponse(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public ErrorResponse(String errorCode, String message, Map<String, String> fieldErrors) {
        this.errorCode = errorCode;
        this.message = message;
        this.fieldErrors = fieldErrors;
    }

    // 기능
    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
