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

에러 응답은 입력값 검증 실패를 제외하면 JSON이 아닌 문자열 한 줄로 반환됩니다. 현재 응답 본문에는 별도의 에러 코드가 없습니다.

<table>
<tr>
<td width="1000">

## 팀원 등록

### `POST` /api/members

새 팀원을 등록합니다. 프로필 이미지는 등록 후 프로필 이미지 업로드 API로 따로 올립니다.

### Request

#### Request Body

| 파라미터 | 타입 | 필수 | 설명 |
|---|---|:---:|---|
| `memberName` | `string` | ✅ | 팀원 이름입니다. 공백 불가, 최대 20자. |
| `memberAge` | `integer` | ✅ | 나이입니다. 1 이상 150 이하. |
| `memberMbti` | `string` | ✅ | MBTI입니다. 대문자 4글자(`[EI][SN][TF][JP]`) 형식. 예: `ENFP` |

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
| `memberId` | `integer` | 생성된 팀원 식별자입니다. |
| `memberName` | `string` | 팀원 이름입니다. |
| `memberAge` | `integer` | 나이입니다. |
| `memberMbti` | `string` | MBTI입니다. |
| `profileImage` | `string` | 프로필 이미지의 S3 key입니다. 등록 직후에는 `null`입니다. |

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
| `400` | - | 입력값 검증에 실패했습니다. `{"필드명": "메시지"}` 형태로 필드별 첫 메시지를 반환합니다. |
| `400` | - | JSON 문법 오류 또는 타입 불일치입니다. `Invalid request body.`를 반환합니다. |

##### 응답 예시

```json
{
  "memberName": "Member name is required.",
  "memberMbti": "MBTI must be a valid 4-letter uppercase type. (e.g. ENFP)"
}
```

</td>
</tr>
</table>

<table>
<tr>
<td width="1000">

## 팀원 조회

### `GET` /api/members/{memberId}

팀원 한 명의 정보를 조회합니다.

### Request

#### Path Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---|---|:---:|---|
| `memberId` | `integer` | ✅ | 팀원 식별자입니다. 숫자만 허용. |

#### 요청 예시

```bash
curl http://localhost:8080/api/members/1
```

### Response

#### 성공 (200 OK)

| 필드 | 타입 | 설명 |
|---|---|---|
| `memberId` | `integer` | 팀원 식별자입니다. |
| `memberName` | `string` | 팀원 이름입니다. |
| `memberAge` | `integer` | 나이입니다. |
| `memberMbti` | `string` | MBTI입니다. |
| `profileImage` | `string` | 프로필 이미지의 S3 key입니다. 이미지가 없으면 `null`입니다. |

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
| `400` | - | `memberId`가 숫자가 아닙니다. `Invalid value for parameter 'memberId'.`를 반환합니다. |
| `404` | - | 존재하지 않는 팀원입니다. `Member not found. id={memberId}`를 반환합니다. |

##### 응답 예시

```text
Member not found. id=1
```

</td>
</tr>
</table>

<table>
<tr>
<td width="1000">

## 프로필 이미지 업로드

### `POST` /api/members/{memberId}/profile-image

팀원의 프로필 이미지를 S3에 업로드합니다. PNG, JPEG, WebP만 허용하며 최대 10MB입니다. 이미 이미지가 있으면 새 이미지로 교체하고 기존 파일은 S3에서 삭제합니다.

### Request

#### Path Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---|---|:---:|---|
| `memberId` | `integer` | ✅ | 팀원 식별자입니다. 숫자만 허용. |

#### Request Body

`multipart/form-data` 형식입니다.

| 파라미터 | 타입 | 필수 | 설명 |
|---|---|:---:|---|
| `file` | `file` | ✅ | 업로드할 이미지입니다. `image/png`, `image/jpeg`, `image/webp`만 허용, 최대 10MB. |

#### 요청 예시

```bash
curl -X POST http://localhost:8080/api/members/1/profile-image \
  -F "file=@profile.png"
```

### Response

#### 성공 (201 Created)

| 필드 | 타입 | 설명 |
|---|---|---|
| `key` | `string` | 저장된 S3 object key입니다. |

##### 응답 예시

```json
{
  "key": "uploads/members/3f2a9c1e-7b4d-4e8a-9f61-2d5c8b0a1e34.png"
}
```

#### 실패

| HTTP 상태 | 에러 코드 | 설명 |
|---|---|---|
| `400` | - | 빈 파일입니다. `File is empty.`를 반환합니다. |
| `400` | - | `file` 파트가 없습니다. `Required part 'file' is missing.`를 반환합니다. |
| `400` | - | multipart 요청이 아닙니다. `Invalid multipart request.`를 반환합니다. |
| `400` | - | `memberId`가 숫자가 아닙니다. `Invalid value for parameter 'memberId'.`를 반환합니다. |
| `404` | - | 존재하지 않는 팀원입니다. `Member not found. id={memberId}`를 반환합니다. |
| `413` | - | 파일이 10MB를 초과했습니다. `File size exceeds the limit.`를 반환합니다. |
| `415` | - | 허용되지 않는 파일 형식입니다. `Only image files (PNG, JPEG, WebP) can be uploaded.`를 반환합니다. |

##### 응답 예시

```text
Only image files (PNG, JPEG, WebP) can be uploaded.
```

</td>
</tr>
</table>

<table>
<tr>
<td width="1000">

## 프로필 이미지 URL 조회

### `GET` /api/members/{memberId}/profile-image

팀원 프로필 이미지를 내려받을 수 있는 S3 Presigned URL을 발급합니다. URL은 발급 후 7일 동안 유효합니다.

### Request

#### Path Parameters

| 파라미터 | 타입 | 필수 | 설명 |
|---|---|:---:|---|
| `memberId` | `integer` | ✅ | 팀원 식별자입니다. 숫자만 허용. |

#### 요청 예시

```bash
curl http://localhost:8080/api/members/1/profile-image
```

### Response

#### 성공 (200 OK)

| 필드 | 타입 | 설명 |
|---|---|---|
| `url` | `string` | 7일간 유효한 Presigned URL입니다. |

##### 응답 예시

```json
{
  "url": "https://{bucket}.s3.ap-northeast-2.amazonaws.com/uploads/members/{UUID}.png?X-Amz-Expires=604800&..."
}
```

#### 실패

| HTTP 상태 | 에러 코드 | 설명 |
|---|---|---|
| `400` | - | `memberId`가 숫자가 아닙니다. `Invalid value for parameter 'memberId'.`를 반환합니다. |
| `404` | - | 존재하지 않는 팀원입니다. `Member not found. id={memberId}`를 반환합니다. |
| `404` | - | 프로필 이미지를 아직 올리지 않았습니다. `profileImage not found.`를 반환합니다. |

##### 응답 예시

```text
profileImage not found.
```

</td>
</tr>
</table>

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

