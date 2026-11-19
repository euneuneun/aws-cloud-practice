package com.awscloudpractice.member.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class MemberCreateRequest {

    // 속성
    @Size(max = 20, message = "Member name must be 20 characters or less.")
    @NotBlank(message = "Member name is required.")
    private String memberName;

    @Min(value = 1, message = "Member age must be at least 1.")
    @Max(value = 150, message = "Member age must be 150 or less.")
    @NotNull(message = "Member age is required.")
    private Integer memberAge;

    @Pattern(
            regexp = "^[EI][SN][TF][JP]$",
            message = "MBTI must be a valid 4-letter uppercase type. (e.g. ENFP)")
    @NotNull(message = "Member MBTI is required.")
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
