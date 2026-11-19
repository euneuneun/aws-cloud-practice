package com.awscloudpractice.member.controller;

import com.awscloudpractice.member.dto.request.MemberCreateRequest;
import com.awscloudpractice.member.dto.response.FileDownloadUrlResponse;
import com.awscloudpractice.member.dto.response.FileUploadResponse;
import com.awscloudpractice.member.dto.response.MemberResponse;
import com.awscloudpractice.member.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    // 속성
    private final MemberService memberService;

    // 생성자
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    // 기능
    @PostMapping
    public ResponseEntity<MemberResponse> createMember(
            @Valid @RequestBody MemberCreateRequest request) {

        MemberResponse response = memberService.createMember(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<MemberResponse> getMember(@PathVariable Long memberId) {

        MemberResponse response = memberService.getMember(memberId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{memberId}/profile-image")
    public ResponseEntity<FileUploadResponse> uploadProfile(
            @PathVariable Long memberId, @RequestParam MultipartFile file) {

        FileUploadResponse response = memberService.uploadProfile(memberId, file);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{memberId}/profile-image")
    public ResponseEntity<FileDownloadUrlResponse> getProfileImage(@PathVariable Long memberId) {

        FileDownloadUrlResponse response = memberService.getProfileImage(memberId);

        return ResponseEntity.ok(response);
    }
}
