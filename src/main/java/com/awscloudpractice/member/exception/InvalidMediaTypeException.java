package com.awscloudpractice.member.exception;

import com.awscloudpractice.common.exception.BusinessException;

public class InvalidMediaTypeException extends BusinessException {

    // 속성
    private MemberErrorCode memberErrorCode;

    // 생성자
    public InvalidMediaTypeException() {
        super(MemberErrorCode.INVALID_MEDIA_TYPE);
    }

    // 기능
    public MemberErrorCode getMemberErrorCode() {
        return memberErrorCode;
    }
}
