package com.awscloudpractice.member.service;

import com.awscloudpractice.member.dto.request.MemberCreateRequest;
import com.awscloudpractice.member.dto.response.MemberResponse;
import com.awscloudpractice.member.entity.Member;
import com.awscloudpractice.member.exception.MemberNotFoundException;
import com.awscloudpractice.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public MemberResponse createMember(MemberCreateRequest request) {

        // 1. 엔티티 생성
        Member member = new Member(request.getMemberName(), request.getMemberAge(), request.getMemberMbti());

        // 2. 엔티티 저장
        Member saveMember = memberRepository.save(member);

        // 3. 엔티티 -> dto 변환
        MemberResponse response = new MemberResponse(
                saveMember.getMemberId(),
                saveMember.getMemberName(),
                saveMember.getMemberAge(),
                saveMember.getMemberMbti()
        );

        return response;
    }

    @Transactional(readOnly = true)
    public MemberResponse getMember(Long memberId) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        MemberResponse response = new MemberResponse(
                member.getMemberId(),
                member.getMemberName(),
                member.getMemberAge(),
                member.getMemberMbti()
        );

        return response;
    }
}