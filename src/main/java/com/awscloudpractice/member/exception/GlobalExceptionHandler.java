package com.awscloudpractice.member.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<String> handleMemberNotFoundException(MemberNotFoundException e) {

        log.error("[API - ERROR] Member not found", e);

        String message = e.getMessage();
        HttpStatus statusCode = e.getStatusCode();

        ResponseEntity<String> response = new ResponseEntity<>(message, statusCode);

        return response;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {

        log.error("Unhandled exception", e);

        ResponseEntity<String> response = new ResponseEntity<>(
                "Internal Server Error",
                HttpStatus.INTERNAL_SERVER_ERROR
        );

        return response;
    }
}
