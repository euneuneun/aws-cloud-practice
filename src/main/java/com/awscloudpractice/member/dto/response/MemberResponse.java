package com.awscloudpractice.member.dto.response;

public class MemberResponse {

    // 속성
    private Long memberId;

    private String memberName;

    private Integer memberAge;

    private String memberMbti;

    // 생성자
    public MemberResponse(Long memberId, String memberName, Integer memberAge, String memberMbti) {
        this.memberId = memberId;
        this.memberName = memberName;
        this.memberAge = memberAge;
        this.memberMbti = memberMbti;
    }

    // 기능
    public Long getMemberId() {
        return memberId;
    }

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
