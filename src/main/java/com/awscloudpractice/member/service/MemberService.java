package com.awscloudpractice.member.service;

import com.awscloudpractice.member.dto.request.MemberCreateRequest;
import com.awscloudpractice.member.dto.response.MemberCreateResponse;
import com.awscloudpractice.member.entity.Member;
import com.awscloudpractice.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class MemberService {

    // 속성
    private final MemberRepository memberRepository;

    // 생성자
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // 기능
    @Transactional
    public MemberCreateResponse createMember(MemberCreateRequest request) {

        // 1. 엔티티 생성
        Member member = new Member(request.getMemberName(), request.getMemberAge(), request.getMemberMbti());

        // 2. 엔티티 저장
        Member saveMember = memberRepository.save(member);

        // 3. 엔티티 -> dto 변환
        MemberCreateResponse response = new MemberCreateResponse(
                saveMember.getMemberId(),
                saveMember.getMemberName(),
                saveMember.getMemberAge(),
                member.getMemberMbti()
        );

        return response;
    }
}