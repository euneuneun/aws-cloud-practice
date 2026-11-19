package com.awscloudpractice.member.exception;

import org.springframework.http.HttpStatus;

public class FilesEmptyException extends RuntimeException {

    // 속성
    private HttpStatus statusCode = HttpStatus.BAD_REQUEST;

    // 생성자
    public FilesEmptyException() {
        super("File is empty.");
    }

    // 기능
    public HttpStatus getStatusCode() {
        return statusCode;
    }
}
