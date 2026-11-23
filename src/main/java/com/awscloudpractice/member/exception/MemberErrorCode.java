package com.awscloudpractice.member.exception;

import com.awscloudpractice.common.exception.ErrorCode;
import org.springframework.http.HttpStatus;

public enum MemberErrorCode implements ErrorCode {
    FILES_EMPTY("MEMBER_FILES_EMPTY", "File is empty.", HttpStatus.BAD_REQUEST),
    INVALID_MEDIA_TYPE(
            "MEMBER_INVALID_MEDIA_TYPE",
            "Only image files (PNG, JPEG, WebP) can be uploaded.",
            HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    MEMBER_NOT_FOUND("MEMBER_NOT_FOUND", "Member not found.", HttpStatus.NOT_FOUND),
    PROFILE_IMAGE_NOT_FOUND(
            "MEMBER_PROFILE_IMAGE_NOT_FOUND", "Profile image not found.", HttpStatus.NOT_FOUND);

    // 속성
    private final String code;
    private final String message;
    private final HttpStatus statusCode;

    // 생성자
    MemberErrorCode(String code, String message, HttpStatus statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    // 기능
    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public HttpStatus getStatusCode() {
        return statusCode;
    }
}
