package com.awscloudpractice.member.repository;

import com.awscloudpractice.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {}
