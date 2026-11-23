package com.awscloudpractice.member.exception;

import com.awscloudpractice.common.exception.BusinessException;

public class FilesEmptyException extends BusinessException {

    // 속성
    private MemberErrorCode memberErrorCode;

    // 생성자
    public FilesEmptyException() {
        super(MemberErrorCode.FILES_EMPTY);
    }

    // 기능
    public MemberErrorCode getMemberErrorCode() {
        return memberErrorCode;
    }
}
