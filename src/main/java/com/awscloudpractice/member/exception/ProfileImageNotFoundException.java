package com.awscloudpractice.member.exception;

import org.springframework.http.HttpStatus;

public class ProfileImageNotFoundException extends RuntimeException {

    // 속성
    private HttpStatus statusCode = HttpStatus.NOT_FOUND;

    // 생성자
    public ProfileImageNotFoundException() {
        super("profileImage not found.");
    }

    // 기능
    public HttpStatus getStatusCode() {
        return statusCode;
    }
}