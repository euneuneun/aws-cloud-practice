# 설계 문서

[← README로 돌아가기](../README.md)

## API

| Method | URI | 설명 |
|---|---|---|
| `POST` | `/api/members` | 팀원 등록 |
| `GET` | `/api/members/{memberId}` | 팀원 조회 |
| `POST` | `/api/members/{memberId}/profile-image` | 프로필 이미지 업로드 (S3) |
| `GET` | `/api/members/{memberId}/profile-image` | 프로필 이미지 Presigned URL 발급 (7일) |
| `GET` | `/actuator/health` | 헬스 체크 |
| `GET` | `/actuator/info` | 팀 이름 (Parameter Store `TEAM_NAME`) |

<details>
<summary><b>요청 / 응답 예시</b></summary>

**POST /api/members** → `201 Created`
```json
// Request
{ "memberName": "홍길동", "memberAge": 27, "memberMbti": "ENFP" }

// Response
{ "memberId": 1, "memberName": "홍길동", "memberAge": 27, "memberMbti": "ENFP", "profileImage": null }
```

**POST /api/members/{memberId}/profile-image** → `201 Created`
- `multipart/form-data`, 파트 이름 `file`
- PNG · JPEG · WebP만 허용, 최대 10MB
- 새 이미지를 올리면 기존 이미지는 S3에서 삭제

```json
{ "key": "uploads/members/{UUID}.png" }
```

**GET /api/members/{memberId}/profile-image** → `200 OK`
```json
{ "url": "https://camp-health-kde-files.s3.ap-northeast-2.amazonaws.com/uploads/members/...?X-Amz-Expires=604800&..." }
```
</details>

<details>
<summary><b>예외 처리</b></summary>

`@RestControllerAdvice`(`GlobalExceptionHandler`)에서 한 곳에 모아 처리합니다.

| 상황 | HTTP | 응답 |
|---|---|---|
| 존재하지 않는 팀원 | 404 | `Member not found. id={id}` |
| 프로필 이미지 미등록 상태에서 조회 | 404 | `profileImage not found.` |
| 존재하지 않는 URL | 404 | `Resource not found.` |
| 입력값 검증 실패 (이름 공백·20자 초과, 나이 1~150 밖, MBTI 형식) | 400 | `{"필드명": "메시지"}` |
| JSON 문법 오류 · 타입 불일치 | 400 | `Invalid request body.` |
| 경로 변수 타입 오류 (`/api/members/abc`) | 400 | `Invalid value for parameter 'memberId'.` |
| 빈 파일 업로드 | 400 | `File is empty.` |
| `file` 파트 누락 | 400 | `Required part 'file' is missing.` |
| multipart가 아닌 요청 | 400 | `Invalid multipart request.` |
| 허용되지 않는 파일 형식 | 415 | `Only image files (PNG, JPEG, WebP) can be uploaded.` |
| 파일 용량 초과 (10MB) | 413 | `File size exceeds the limit.` |
| 그 밖의 예외 | 500 | `Internal Server Error` (상세는 로그에만) |

**로그 전략**
- 모든 API 요청: `LoggingInterceptor` → `INFO [API - LOG] Method: POST, URL: /api/members`
- 예외 발생: `ERROR [API - ERROR] ...` + 스택트레이스
</details>


## 테이블 설계

**members**

| 컬럼 | 타입 | 제약 조건 | 설명 |
| --- | --- | --- | --- |
| `member_id` | BIGINT | PK, AUTO_INCREMENT | 팀원 ID |
| `member_name` | VARCHAR(20) | NOT NULL | 팀원 이름 |
| `member_age` | INT | NOT NULL | 팀원 나이 |
| `member_mbti` | VARCHAR(4) | NOT NULL | 팀원 MBTI |
| `profile_image` | VARCHAR(255) | NULL | 프로필 이미지 S3 key |


## 프로젝트 구조

```
src/main/java/com/awscloudpractice
├── AwsCloudPracticeApplication.java
├── common
│   ├── config          # Webconfig: 인터셉터 등록
│   ├── exception       # GlobalExceptionHandler: 전역 예외 처리
│   ├── interceptor     # LoggingInterceptor: [API - LOG] 요청 로그
│   └── s3              # S3Service: 업로드, 삭제, Presigned URL 생성
└── member
    ├── controller      # MemberController
    ├── dto
    │   ├── request     # MemberCreateRequest
    │   └── response    # MemberResponse, FileUploadResponse, FileDownloadUrlResponse
    ├── entity          # Member
    ├── exception       # MemberNotFound, ProfileImageNotFound, FilesEmpty, InvalidMediaType
    ├── repository      # MemberRepository
    └── service         # MemberService
src/main/resources
├── application.yaml        # 공통: 리전, S3 버킷, 업로드 제한, Actuator 노출
├── application-local.yaml  # H2
└── application-prod.yaml   # MySQL + Parameter Store
Dockerfile
docs/                       # 과제 제출물, 인프라 상세, 스크린샷
```

