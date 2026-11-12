package com.awscloudpractice.member.exception;

import org.springframework.http.HttpStatus;

public class MemberNotFoundException extends RuntimeException {

    // 속성
    private HttpStatus statusCode = HttpStatus.NOT_FOUND;

    // 생성자
    public MemberNotFoundException() {
        super("Member Not Found");
    }

    // 기능
    @Override
    public String getMessage() {
        return super.getMessage();
    }

    public HttpStatus getStatusCode() {
        return statusCode;
    }
}
