package com.awscloudpractice.member.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class MemberCreateRequest {

    // 속성
    @Size(max = 20, message = "20자 이내로 작성해주세요.")
    @NotNull(message = "팀원 이름을 입력해주세요.")
    private String memberName;

    @NotNull(message = "팀원 나이를 입력해주세요.")
    private Integer memberAge;

    @Size(max = 4, message = "4자 이내로 작성해주세요.")
    @Pattern(regexp = "^[EI][SN][TF][JP]$", message = "올바른 MBTI를 대문자로 입력해주세요. (예: ENFP)")
    @NotNull(message = "팀원 mbti를 입력해주세요.")
    private String memberMbti;

    // 생성자

    // 기능
    public String getMemberName() {
        return memberName;
    }

    public Integer getMemberAge() {
        return memberAge;
    }

    public String getMemberMbti() {
        return memberMbti;
    }
}
