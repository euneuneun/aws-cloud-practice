package com.awscloudpractice.common.exception;

import com.awscloudpractice.member.exception.MemberNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<String> handleMemberNotFoundException(MemberNotFoundException e) {

        log.error("[API - ERROR] Member not found", e);

        String message = e.getMessage();
        HttpStatus statusCode = e.getStatusCode();

        return ResponseEntity.status(statusCode).body(message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException e) {

        Map<String, String> errors = new LinkedHashMap<>();

        BindingResult bindingResult = e.getBindingResult();            // 검증 결과 전체
        List<FieldError> fieldErrors = bindingResult.getFieldErrors(); // 필드 에러 목록

        for (FieldError error : fieldErrors) {
            String field = error.getField();                           // 필드명: "mbti"
            String message = error.getDefaultMessage();                // 메시지: "must not be null"
            errors.putIfAbsent(field, message);                        // 필드별 첫 메시지만 저장
        }

        log.error("[API - ERROR] Validation failed: {}", errors, e);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {

        log.error("Unhandled exception", e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
    }
}
