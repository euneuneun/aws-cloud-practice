package com.awscloudpractice.member.exception;

import org.springframework.http.HttpStatus;

public class InvalidMediaTypeException extends RuntimeException{

    // 속성
    private HttpStatus statusCode = HttpStatus.UNSUPPORTED_MEDIA_TYPE;

    // 생성자
    public InvalidMediaTypeException() {
        super("Only image files (PNG, JPEG, WebP) can be uploaded.");
    }

    // 기능
    public HttpStatus getStatusCode() {
        return statusCode;
    }
}
