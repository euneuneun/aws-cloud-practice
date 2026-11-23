package com.awscloudpractice.member.exception;

import com.awscloudpractice.common.exception.BusinessException;

public class ProfileImageNotFoundException extends BusinessException {

    // 속성
    private MemberErrorCode memberErrorCode;

    // 생성자
    public ProfileImageNotFoundException() {
        super(MemberErrorCode.PROFILE_IMAGE_NOT_FOUND);
    }

    // 기능
    public MemberErrorCode getMemberErrorCode() {
        return memberErrorCode;
    }
}
