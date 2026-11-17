package com.awscloudpractice.member.entity;

import jakarta.persistence.*;

@Table(name = "members")
@Entity
public class Member {

    // 속성
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(nullable = false, length = 20)
    private String memberName;

    @Column(nullable = false)
    private Integer memberAge;

    @Column(nullable = false, length = 4)
    private String memberMbti;

    @Column(nullable = true)
    private String profileImage;

    // 생성자
    public Member() {
    }

    public Member(String memberName, Integer memberAge, String memberMbti) {
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

    public String getProfileImage() {
        return profileImage;
    }

    public void uploadProfile(String profileImage) {
        this.profileImage = profileImage;
    }
}
