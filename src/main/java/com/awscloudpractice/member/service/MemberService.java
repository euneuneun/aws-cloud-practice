package com.awscloudpractice.member.service;

import com.awscloudpractice.common.s3.S3Service;
import com.awscloudpractice.member.dto.request.MemberCreateRequest;
import com.awscloudpractice.member.dto.response.FileDownloadUrlResponse;
import com.awscloudpractice.member.dto.response.FileUploadResponse;
import com.awscloudpractice.member.dto.response.MemberResponse;
import com.awscloudpractice.member.entity.Member;
import com.awscloudpractice.member.exception.FilesEmptyException;
import com.awscloudpractice.member.exception.InvalidMediaTypeException;
import com.awscloudpractice.member.exception.MemberNotFoundException;
import com.awscloudpractice.member.repository.MemberRepository;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MemberService {

    // 속성
    private static final Set<String> ALLOWED_IMAGE_TYPES =
            Set.of("image/png", "image/jpeg", "image/webp");

    // Presigned URL 유효기간
    // private static final Duration PROFILE_IMAGE_URL_EXPIRATION = Duration.ofDays(7);

    private final MemberRepository memberRepository;
    private final S3Service s3Service;
    private final String cloudFrontDomain;

    // 생성자
    public MemberService(
            MemberRepository memberRepository,
            S3Service s3Service,
            @Value("${cloudfront.domain}") String cloudFrontDomain) {
        this.memberRepository = memberRepository;
        this.s3Service = s3Service;
        this.cloudFrontDomain = cloudFrontDomain;
    }

    // 기능
    @Transactional
    public MemberResponse createMember(MemberCreateRequest request) {

        // 1. 엔티티 생성
        Member member =
                new Member(
                        request.getMemberName(), request.getMemberAge(), request.getMemberMbti());

        // 2. 엔티티 저장
        Member saveMember = memberRepository.save(member);

        // 3. 엔티티 -> dto 변환
        MemberResponse response =
                new MemberResponse(
                        saveMember.getMemberId(),
                        saveMember.getMemberName(),
                        saveMember.getMemberAge(),
                        saveMember.getMemberMbti(),
                        saveMember.getProfileImage());

        return response;
    }

    @Transactional(readOnly = true)
    public MemberResponse getMember(Long memberId) {

        Member member =
                memberRepository
                        .findById(memberId)
                        .orElseThrow(() -> new MemberNotFoundException());

        MemberResponse response =
                new MemberResponse(
                        member.getMemberId(),
                        member.getMemberName(),
                        member.getMemberAge(),
                        member.getMemberMbti(),
                        member.getProfileImage());

        return response;
    }

    @Transactional
    public FileUploadResponse uploadProfile(Long memberId, MultipartFile file) {

        // 1. 회원 조회
        Member member =
                memberRepository
                        .findById(memberId)
                        .orElseThrow(() -> new MemberNotFoundException());

        // 2. 빈 파일 검사
        if (file.isEmpty()) {
            throw new FilesEmptyException();
        }

        // 3. 이미지 타입 검사
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new InvalidMediaTypeException();
        }

        // 4. 기존 프로필 사진 key 보관 (새 업로드 성공 후 삭제하기 위해)
        String oldKey = member.getProfileImage();

        // 5. 저장할 파일 이름(key) 생성
        String ext = contentType.substring(contentType.indexOf("/") + 1);
        String key = "uploads/members/" + UUID.randomUUID() + "." + ext;

        // 6. S3 업로드
        s3Service.s3Upload(key, file);

        // 7. 회원 엔티티에 key 저장
        member.uploadProfile(key);

        // 8. 변경 전 key 삭제
        if (oldKey != null) {
            s3Service.s3Delete(oldKey);
        }

        // 9. 엔티티 -> dto 변환
        FileUploadResponse response = new FileUploadResponse(key);

        return response;
    }

    @Transactional(readOnly = true)
    public FileDownloadUrlResponse getProfileImage(Long memberId) {

        // 1. 회원 조회
        Member member =
                memberRepository
                        .findById(memberId)
                        .orElseThrow(() -> new MemberNotFoundException());

        // 2. 회원 프로필 사진 key값 조회
        String key = member.getProfileImageOrThrow();

        // 3. Presigned URL 만들기 (이전 방식)
        // String presignedUrl = s3Service.createPresignedGetUrl(key, PROFILE_IMAGE_URL_EXPIRATION);

        // 3. CloudFront URL 만들기 (오리진 경로가 비어 있어 URL 경로 = S3 key)
        String imageUrl = "https://" + cloudFrontDomain + "/" + key;

        // 4. 엔티티 -> dto 변환
        FileDownloadUrlResponse response = new FileDownloadUrlResponse(imageUrl);

        return response;
    }
}
