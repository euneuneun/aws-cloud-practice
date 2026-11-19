package com.awscloudpractice.common.exception;

import com.awscloudpractice.member.exception.FilesEmptyException;
import com.awscloudpractice.member.exception.InvalidMediaTypeException;
import com.awscloudpractice.member.exception.MemberNotFoundException;
import com.awscloudpractice.member.exception.ProfileImageNotFoundException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 모든 컨트롤러에서 발생한 예외를 한 곳에서 처리하는 전역 예외 처리기.
 *
 * <p>예외 종류에 맞는 HTTP 상태 코드와 메시지로 응답하고, 모든 예외는 ERROR 레벨 로그와 스택 트레이스를 남긴다.
 *
 * <ul>
 *   <li>400 Bad Request: 빈 파일 업로드, 파일 파트 누락, 잘못된 multipart 요청, 검증 실패, 요청 본문 파싱 실패, 파라미터 타입 오류
 *   <li>404 Not Found: 존재하지 않는 팀원, 존재하지 않는 URL
 *   <li>413 Content Too Large: 업로드 크기 제한 초과
 *   <li>415 Unsupported Media Type: 허용되지 않는 파일 형식 업로드
 *   <li>500 Internal Server Error: 그 밖에 처리되지 않은 모든 예외
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 요청한 id의 팀원이 없을 때 처리한다.
     *
     * @param e 조회하지 못한 팀원 id가 담긴 예외
     * @return 404 상태 코드와 예외 메시지 (예: {@code "Member not found. id=5"})
     */
    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<String> handleMemberNotFoundException(MemberNotFoundException e) {

        log.error("[API - ERROR] Member not found", e);

        String message = e.getMessage();
        HttpStatus statusCode = e.getStatusCode();

        return ResponseEntity.status(statusCode).body(message);
    }

    /**
     * 업로드한 파일이 비어 있을 때 처리한다.
     *
     * @param e 빈 파일을 업로드했을 때 발생하는 예외
     * @return 400 상태 코드와 예외 메시지 (예: {@code "File is empty."})
     */
    @ExceptionHandler(FilesEmptyException.class)
    public ResponseEntity<String> handleFilesEmptyException(FilesEmptyException e) {

        log.error("[API - ERROR] File is empty.", e);

        String message = e.getMessage();
        HttpStatus statusCode = e.getStatusCode();

        return ResponseEntity.status(statusCode).body(message);
    }

    /**
     * 업로드한 파일의 형식이 맞지 않을 때 처리한다.
     *
     * @param e 허용되지 않는 파일 형식을 업로드했을 때 발생하는 예외
     * @return 415 상태 코드와 예외 메시지 (예: {@code "Only image files (PNG, JPEG, WebP) can be uploaded."})
     */
    @ExceptionHandler(InvalidMediaTypeException.class)
    public ResponseEntity<String> handleInvalidMediaTypeException(InvalidMediaTypeException e) {

        log.error("[API - ERROR] Only image files (PNG, JPEG, WebP) can be uploaded.", e);

        String message = e.getMessage();
        HttpStatus statusCode = e.getStatusCode();

        return ResponseEntity.status(statusCode).body(message);
    }

    /**
     * 프로필 사진이 존재하지 않을 때 처리한다.
     *
     * @param e 프로필 사진이 존재하지 않을 때 발생하는 예외
     * @return 404 상태 코드와 예외 메세지 (예: {@code "profileImage not found."})
     */
    @ExceptionHandler(ProfileImageNotFoundException.class)
    public ResponseEntity<String> handleProfileImageNotFoundException(
            ProfileImageNotFoundException e) {

        log.error("[API - ERROR] profileImage not found.", e);

        String message = e.getMessage();
        HttpStatus statusCode = e.getStatusCode();

        return ResponseEntity.status(statusCode).body(message);
    }

    /**
     * {@code @Valid} 요청 본문 검증에 실패했을 때 처리한다.
     *
     * <p>한 필드가 여러 검증에 걸리면 그중 하나의 메시지만 담는다.
     *
     * @param e 필드별 검증 실패 정보가 담긴 예외
     * @return 400 상태 코드와 "필드명: 에러 메시지" 형태의 Map (예: {@code {"memberMbti": "Member MBTI is
     *     required."}})
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(
            MethodArgumentNotValidException e) {

        Map<String, String> errors = new LinkedHashMap<>();

        BindingResult bindingResult = e.getBindingResult(); // 검증 결과 전체
        List<FieldError> fieldErrors = bindingResult.getFieldErrors(); // 필드 에러 목록

        for (FieldError error : fieldErrors) {
            String field = error.getField(); // 필드명: "memberMbti"
            String message = error.getDefaultMessage(); // 메시지: "Member MBTI is required."
            errors.putIfAbsent(field, message); // 필드별 첫 메시지만 저장
        }

        log.error("[API - ERROR] Validation failed: {}", errors, e);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    /**
     * 요청 본문을 읽을 수 없을 때 처리한다.
     *
     * <p>JSON 문법 오류나 타입이 맞지 않는 값(예: {@code "memberAge": "abc"})이 해당된다.
     *
     * @param e 요청 본문 파싱 실패 예외
     * @return 400 상태 코드와 {@code "Invalid request body."}
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e) {

        log.error("[API - ERROR] Invalid request body", e);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request body.");
    }

    /**
     * 경로 변수나 쿼리 파라미터의 타입이 맞지 않을 때 처리한다.
     *
     * <p>예: {@code GET /api/members/abc} (id는 숫자여야 함)
     *
     * @param e 타입 변환에 실패한 파라미터 정보가 담긴 예외
     * @return 400 상태 코드와 잘못된 파라미터 이름이 담긴 메시지
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e) {

        log.error("[API - ERROR] Invalid parameter type", e);

        String message = "Invalid value for parameter '" + e.getName() + "'.";

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    /**
     * 존재하지 않는 URL로 요청했을 때 처리한다.
     *
     * @param e 요청 경로에 맞는 핸들러나 리소스가 없을 때 발생하는 예외
     * @return 404 상태 코드와 {@code "Resource not found."}
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<String> handleNoResourceFoundException(NoResourceFoundException e) {

        log.error("[API - ERROR] Resource not found", e);

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Resource not found.");
    }

    /**
     * 업로드한 파일이 허용 크기({@code max-file-size})를 넘었을 때 처리한다.
     *
     * @param e 업로드 크기 제한을 넘었을 때 발생하는 예외
     * @return 413 상태 코드와 {@code "File size exceeds the limit."}
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException e) {

        log.error("[API - ERROR] File size exceeds the limit", e);

        return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE)
                .body("File size exceeds the limit.");
    }

    /**
     * multipart 요청에 필요한 파일 파트가 없을 때 처리한다.
     *
     * <p>예: {@code file} 파라미터 없이 업로드 요청
     *
     * @param e 누락된 파트 이름이 담긴 예외
     * @return 400 상태 코드와 누락된 파트 이름이 담긴 메시지
     */
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<String> handleMissingServletRequestPartException(
            MissingServletRequestPartException e) {

        log.error("[API - ERROR] Missing request part", e);

        String message = "Required part '" + e.getRequestPartName() + "' is missing.";

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    /**
     * multipart 요청이 아니거나 multipart 본문을 읽을 수 없을 때 처리한다.
     *
     * <p>예: 파일 업로드 API에 {@code Content-Type: application/json}으로 요청
     *
     * @param e multipart 처리 실패 예외
     * @return 400 상태 코드와 {@code "Invalid multipart request."}
     */
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<String> handleMultipartException(MultipartException e) {

        log.error("[API - ERROR] Invalid multipart request", e);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid multipart request.");
    }

    /**
     * 위 핸들러에서 처리하지 못한 나머지 모든 예외를 처리한다.
     *
     * <p>내부 정보가 노출되지 않도록 응답에는 고정된 메시지만 담고, 상세 내용은 로그로만 남긴다.
     *
     * @param e 처리되지 않은 예외
     * @return 500 상태 코드와 {@code "Internal Server Error"}
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {

        log.error("[API - ERROR] Unhandled exception", e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal Server Error");
    }
}
