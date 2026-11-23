package com.awscloudpractice.member.exception;

import com.awscloudpractice.common.exception.BusinessException;

public class MemberNotFoundException extends BusinessException {

    // 속성
    private MemberErrorCode memberErrorCode;

    // 생성자
    public MemberNotFoundException() {
        super(MemberErrorCode.MEMBER_NOT_FOUND);
    }

    // 기능
    public MemberErrorCode getMemberErrorCode() {
        return memberErrorCode;
    }
}
