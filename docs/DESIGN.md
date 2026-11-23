# 설계 문서

[← README로 돌아가기](../README.md)

## 목차

* 🔗 [API](#api) — 엔드포인트 목록, 요청 / 응답 명세
* 🗄️ [테이블 설계](#테이블-설계) — 테이블 구조
* 📁 [프로젝트 구조](#프로젝트-구조) — 패키지 구성

## API

| Method | URI | 설명 |
|---|---|---|
| `POST` | `/api/members` | [팀원 등록](#팀원-등록) |
| `GET` | `/api/members/{memberId}` | [팀원 조회](#팀원-조회) |
| `POST` | `/api/members/{memberId}/profile-image` | [프로필 이미지 업로드 (S3)](#프로필-이미지-업로드) |
| `GET` | `/api/members/{memberId}/profile-image` | [프로필 이미지 Presigned URL 발급 (7일)](#프로필-이미지-url-조회) |
| `GET` | `/actuator/health` | 헬스 체크 |
| `GET` | `/actuator/info` | 팀 이름 (Parameter Store `TEAM_NAME`) |

에러 응답은 모두 같은 JSON 형태(`errorCode`, `message`)로 반환됩니다. 입력값 검증에 실패한 경우에만 `fieldErrors`가 추가됩니다. 에러 코드 전체 목록은 아래 **예외 처리** 항목에 있습니다.

```json
{
  "errorCode": "MEMBER_NOT_FOUND",
  "message": "Member not found."
}
```

## 팀원 등록
<table>
<tr>
<td width="1000">

### `POST` /api/members

새 팀원을 등록합니다. 프로필 이미지는 등록 후 프로필 이미지 업로드 API로 따로 올립니다.

### Request

#### Request Body

| 파라미터 | 타입 | 필수 | 설명 |
|---|---|:---:|---|
| `memberName` | `string` | ✅ | 팀원 이름<br>• `null`, 빈 문자열, 공백만 있는 값 불가<br>• 최대 20자 (한글도 1글자로 계산)<br> |
| `memberAge` | `integer` | ✅ | 나이<br>• 1 이상 150 이하 정수<br>|
| `memberMbti` | `string` | ✅ | MBTI<br>• 정규식 `^[EI][SN][TF][JP]$`, 대문자 4글자 (예: `ENFP`, `ISTJ`)<br>• 소문자(`enfp`)·빈 문자열 불가<br> |

#### 요청 예시

```bash
curl -X POST http://localhost:8080/api/members \
  -H "Content-Type: application/json" \
  -d '{"memberName": "홍길동", "memberAge": 27, "memberMbti": "ENFP"}'
```

### Response

#### 성공 (201 Created)

| 필드 | 타입 | 설명 |
|---|---|---|
| `memberId` | `integer` | 팀원 ID |
| `memberName` | `string` | 이름 |
| `memberAge` | `integer` | 나이 |
| `memberMbti` | `string` | MBTI |
| `profileImage` | `string` | 프로필 이미지 S3 key. 등록 직후에는 `null` |

##### 응답 예시

```json
{
  "memberId": 1,
  "memberName": "홍길동",
  "memberAge": 27,
  "memberMbti": "ENFP",
  "profileImage": null
}
```

#### 실패

| HTTP 상태 | 에러 코드 | 설명 |
|---|---|---|
| `400` | `COMMON_INVALID_INPUT_VALUE` | 입력값 검증에 실패한 경우. `fieldErrors`에 필드별 첫 메시지를 담습니다. |
| `400` | `COMMON_INVALID_INPUT_VALUE` | JSON 문법이 틀리거나 타입이 맞지 않는 경우 |

##### 응답 예시

```json
{
  "errorCode": "COMMON_INVALID_INPUT_VALUE",
  "message": "Invalid input value.",
  "fieldErrors": {
    "memberName": "Member name is required.",
    "memberMbti": "MBTI must be a valid 4-letter uppercase type. (e.g. ENFP)"
  }
}
```

</td>
</tr>
</table>


## 팀원 조회
<table>
<tr>
<td width="1000">

### `GET` /api/members/{memberId}

팀원 한 명의 정보를 조회합니다.

### Request

#### Path Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---|---|:---:|---|
| `memberId` | `integer` | ✅ | 팀원 ID |

#### 요청 예시

```bash
curl http://localhost:8080/api/members/1
```

### Response

#### 성공 (200 OK)

| 필드 | 타입 | 설명 |
|---|---|---|
| `memberId` | `integer` | 팀원 ID |
| `memberName` | `string` | 이름 |
| `memberAge` | `integer` | 나이 |
| `memberMbti` | `string` | MBTI |
| `profileImage` | `string` | 프로필 이미지 S3 key. 이미지를 올리지 않았으면 `null` |

##### 응답 예시

```json
{
  "memberId": 1,
  "memberName": "홍길동",
  "memberAge": 27,
  "memberMbti": "ENFP",
  "profileImage": "uploads/members/3f2a9c1e-7b4d-4e8a-9f61-2d5c8b0a1e34.png"
}
```

#### 실패

| HTTP 상태 | 에러 코드 | 설명 |
|---|---|---|
| `400` | `COMMON_INVALID_INPUT_VALUE` | `memberId`가 숫자가 아닌 경우 |
| `404` | `MEMBER_NOT_FOUND` | 존재하지 않는 팀원인 경우 |

##### 응답 예시

```json
{
  "errorCode": "MEMBER_NOT_FOUND",
  "message": "Member not found."
}
```

</td>
</tr>
</table>

## 프로필 이미지 업로드
<table>
<tr>
<td width="1000">

### `POST` /api/members/{memberId}/profile-image

팀원의 프로필 이미지를 S3에 업로드합니다. PNG, JPEG, WebP만 허용하며 최대 10MB입니다. 이미 이미지가 있으면 새 이미지로 교체하고 기존 파일은 S3에서 삭제합니다.

### Request

#### Path Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---|---|:---:|---|
| `memberId` | `integer` | ✅ | 팀원 ID |

#### Request Body

`multipart/form-data` 형식입니다.

| 파라미터 | 타입 | 필수 | 설명 |
|---|---|:---:|---|
| `file` | `file` | ✅ | 업로드할 프로필 이미지<br>• 파트 이름은 반드시 `file`<br>• 허용 형식: `image/png`, `image/jpeg`, `image/webp`<br>• 형식은 파일 내용이 아니라 파트의 `Content-Type`으로 판단<br>• 최대 10MB<br>• 저장 key: `uploads/members/{UUID}.{확장자}` (확장자는 Content-Type 뒷부분, JPEG는 `.jpeg`)<br>• 기존 이미지가 있으면 업로드 성공 후 S3에서 삭제 |

#### 요청 예시

```bash
curl -X POST http://localhost:8080/api/members/1/profile-image \
  -F "file=@profile.png"
```

### Response

#### 성공 (201 Created)

| 필드 | 타입 | 설명 |
|---|---|---|
| `key` | `string` | 저장된 S3 key (`uploads/members/{UUID}.{확장자}`) |

##### 응답 예시

```json
{
  "key": "uploads/members/3f2a9c1e-7b4d-4e8a-9f61-2d5c8b0a1e34.png"
}
```

#### 실패

| HTTP 상태 | 에러 코드 | 설명 |
|---|---|---|
| `400` | `MEMBER_FILES_EMPTY` | 빈 파일을 올린 경우 |
| `400` | `COMMON_INVALID_INPUT_VALUE` | `file` 파트가 없는 경우 |
| `400` | `COMMON_INVALID_INPUT_VALUE` | multipart 요청이 아닌 경우 |
| `400` | `COMMON_INVALID_INPUT_VALUE` | `memberId`가 숫자가 아닌 경우 |
| `404` | `MEMBER_NOT_FOUND` | 존재하지 않는 팀원인 경우 |
| `413` | `COMMON_FILE_SIZE_EXCEEDED` | 파일이 10MB를 초과한 경우 |
| `415` | `MEMBER_INVALID_MEDIA_TYPE` | 허용되지 않는 파일 형식인 경우 |

##### 응답 예시

```json
{
  "errorCode": "MEMBER_INVALID_MEDIA_TYPE",
  "message": "Only image files (PNG, JPEG, WebP) can be uploaded."
}
```

</td>
</tr>
</table>

## 프로필 이미지 URL 조회
<table>
<tr>
<td width="1000">

### `GET` /api/members/{memberId}/profile-image

팀원 프로필 이미지를 내려받을 수 있는 S3 Presigned URL을 발급합니다. URL은 발급 후 7일 동안 유효합니다.

### Request

#### Path Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---|---|:---:|---|
| `memberId` | `integer` | ✅ | 팀원 ID |

#### 요청 예시

```bash
curl http://localhost:8080/api/members/1/profile-image
```

### Response

#### 성공 (200 OK)

| 필드 | 타입 | 설명 |
|---|---|---|
| `url` | `string` | 7일간 유효한 Presigned URL |

##### 응답 예시

```json
{
  "url": "https://{bucket}.s3.ap-northeast-2.amazonaws.com/uploads/members/{UUID}.png?X-Amz-Expires=604800&..."
}
```

#### 실패

| HTTP 상태 | 에러 코드 | 설명 |
|---|---|---|
| `400` | `COMMON_INVALID_INPUT_VALUE` | `memberId`가 숫자가 아닌 경우 |
| `404` | `MEMBER_NOT_FOUND` | 존재하지 않는 팀원인 경우 |
| `404` | `MEMBER_PROFILE_IMAGE_NOT_FOUND` | 프로필 이미지를 아직 올리지 않은 경우 |

##### 응답 예시

```json
{
  "errorCode": "MEMBER_PROFILE_IMAGE_NOT_FOUND",
  "message": "Profile image not found."
}
```

</td>
</tr>
</table>

<details>
<summary><b>예외 처리</b></summary>

`@RestControllerAdvice`(`GlobalExceptionHandler`)에서 한 곳에 모아 처리합니다.

**구조**
- `ErrorCode` (인터페이스): 에러 코드 · 메시지 · HTTP 상태를 꺼내는 공통 규칙
- `CommonErrorCode` (enum): Spring이 던지는 공통 예외용 코드 (`COMMON_` 접두사)
- `MemberErrorCode` (enum): 팀원 도메인 예외용 코드 (`MEMBER_` 접두사)
- `BusinessException`: 도메인 예외의 부모 클래스. `ErrorCode`를 담고 있어, 핸들러 하나(`handleBusinessException`)로 모든 도메인 예외를 처리합니다.
- `ErrorResponse`: 모든 에러 응답 형태 (`errorCode`, `message`, 검증 실패 시 `fieldErrors`). 값이 null인 항목은 응답에서 빠집니다.

| 상황 | HTTP | 에러 코드 | 메시지 |
|---|---|---|---|
| 존재하지 않는 팀원 | 404 | `MEMBER_NOT_FOUND` | `Member not found.` |
| 프로필 이미지 미등록 상태에서 조회 | 404 | `MEMBER_PROFILE_IMAGE_NOT_FOUND` | `Profile image not found.` |
| 빈 파일 업로드 | 400 | `MEMBER_FILES_EMPTY` | `File is empty.` |
| 허용되지 않는 파일 형식 | 415 | `MEMBER_INVALID_MEDIA_TYPE` | `Only image files (PNG, JPEG, WebP) can be uploaded.` |
| 입력값 검증 실패 (이름 공백·20자 초과, 나이 1~150 밖, MBTI 형식) | 400 | `COMMON_INVALID_INPUT_VALUE` | `Invalid input value.` + `fieldErrors` |
| JSON 문법 오류 · 타입 불일치 | 400 | `COMMON_INVALID_INPUT_VALUE` | `Invalid input value.` |
| 경로 변수 타입 오류 (`/api/members/abc`) | 400 | `COMMON_INVALID_INPUT_VALUE` | `Invalid input value.` |
| `file` 파트 누락 | 400 | `COMMON_INVALID_INPUT_VALUE` | `Invalid input value.` |
| multipart가 아닌 요청 | 400 | `COMMON_INVALID_INPUT_VALUE` | `Invalid input value.` |
| 존재하지 않는 URL | 404 | `COMMON_RESOURCE_NOT_FOUND` | `Resource not found.` |
| 파일 용량 초과 (10MB) | 413 | `COMMON_FILE_SIZE_EXCEEDED` | `File size exceeds the limit.` |
| 그 밖의 예외 | 500 | `COMMON_INTERNAL_SERVER_ERROR` | `Internal Server Error` (상세는 로그에만) |

400 공통 예외는 클라이언트 입장에서 모두 "요청 값이 잘못됨"이라 코드 하나로 합쳤고, 어떤 예외였는지는 로그의 예외 클래스로 확인합니다.

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
│   ├── exception       # GlobalExceptionHandler, BusinessException, ErrorCode, CommonErrorCode
│   ├── interceptor     # LoggingInterceptor: [API - LOG] 요청 로그
│   ├── response        # ErrorResponse: 공통 에러 응답 형태
│   └── s3              # S3Service: 업로드, 삭제, Presigned URL 생성
└── member
    ├── controller      # MemberController
    ├── dto
    │   ├── request     # MemberCreateRequest
    │   └── response    # MemberResponse, FileUploadResponse, FileDownloadUrlResponse
    ├── entity          # Member
    ├── exception       # MemberErrorCode, MemberNotFound, ProfileImageNotFound, FilesEmpty, InvalidMediaType
    ├── repository      # MemberRepository
    └── service         # MemberService
src/main/resources
├── application.yaml        # 공통: 리전, S3 버킷, 업로드 제한, Actuator 노출
├── application-local.yaml  # H2
└── application-prod.yaml   # MySQL + Parameter Store
Dockerfile
docs/                       # 과제 제출물, 인프라 상세, 스크린샷
```

