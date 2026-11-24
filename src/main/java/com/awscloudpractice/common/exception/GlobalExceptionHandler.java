package com.awscloudpractice.common.exception;

import com.awscloudpractice.common.response.ErrorResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
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
 * <ul>
 *   <li>도메인 예외({@link BusinessException}): 각 도메인 ErrorCode에 정의된 상태 코드와 메시지
 *   <li>400 Bad Request: 검증 실패, 요청 본문 파싱 실패, 파라미터 타입 오류, 파일 파트 누락, 잘못된 multipart 요청
 *   <li>404 Not Found: 존재하지 않는 URL
 *   <li>413 Content Too Large: 업로드 크기 제한 초과
 *   <li>500 Internal Server Error: 그 밖에 처리되지 않은 모든 예외
 * </ul>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 도메인에서 던진 {@link BusinessException}을 처리한다.
     *
     * @param e ErrorCode가 담긴 비즈니스 예외
     * @return ErrorCode의 상태 코드와 ErrorResponse (예: {@code {"errorCode": "MEMBER_NOT_FOUND",
     *     "message": "Member not found."}})
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {

        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse response = new ErrorResponse(errorCode.getCode(), errorCode.getMessage());

        return ResponseEntity.status(errorCode.getStatusCode()).body(response);
    }

    /**
     * {@code @Valid} 요청 본문 검증에 실패했을 때 처리한다.
     *
     * <p>한 필드가 여러 검증에 걸리면 그중 하나의 메시지만 담는다.
     *
     * @param e 필드별 검증 실패 정보가 담긴 예외
     * @return 400 상태 코드와 필드별 에러가 담긴 ErrorResponse (예: {@code "fieldErrors": {"memberMbti": "Member
     *     MBTI is required."}})
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException e) {

        Map<String, String> errors = new LinkedHashMap<>();

        // 검증 결과 전체
        BindingResult bindingResult = e.getBindingResult();
        // 필드 에러 목록
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();

        for (FieldError error : fieldErrors) {
            // 필드명: "memberMbti"
            String field = error.getField();
            // 메시지: "Member MBTI is required."
            String message = error.getDefaultMessage();
            // 필드별 첫 메시지만 저장
            errors.putIfAbsent(field, message);
        }

        CommonErrorCode errorCode = CommonErrorCode.INVALID_INPUT_VALUE;
        ErrorResponse response =
                new ErrorResponse(errorCode.getCode(), errorCode.getMessage(), errors);

        return ResponseEntity.status(errorCode.getStatusCode()).body(response);
    }

    /**
     * 요청 값이 잘못되어 400 Bad Request로 응답해야 하는 예외를 한 번에 처리한다.
     *
     * <ul>
     *   <li>{@link HttpMessageNotReadableException}: JSON 문법 오류, 타입이 맞지 않는 값 (예: {@code
     *       "memberAge": "abc"})
     *   <li>{@link MethodArgumentTypeMismatchException}: 경로 변수·쿼리 파라미터 타입 오류 (예: {@code GET
     *       /api/members/abc})
     *   <li>{@link MissingServletRequestPartException}: 업로드 요청에 {@code file} 파트가 없음
     *   <li>{@link MultipartException}: multipart 요청이 아니거나 본문을 읽을 수 없음
     * </ul>
     *
     * <p>어떤 예외였는지는 로그에 남는 예외 클래스 이름으로 확인한다.
     */
    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class,
        MissingServletRequestPartException.class,
        MultipartException.class
    })
    public ResponseEntity<ErrorResponse> handleInvalidInputException(Exception e) {

        CommonErrorCode errorCode = CommonErrorCode.INVALID_INPUT_VALUE;
        ErrorResponse response = new ErrorResponse(errorCode.getCode(), errorCode.getMessage());

        return ResponseEntity.status(errorCode.getStatusCode()).body(response);
    }

    /** 존재하지 않는 URL로 요청했을 때 처리한다. */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(
            NoResourceFoundException e) {

        CommonErrorCode errorCode = CommonErrorCode.RESOURCE_NOT_FOUND;
        ErrorResponse response = new ErrorResponse(errorCode.getCode(), errorCode.getMessage());

        return ResponseEntity.status(errorCode.getStatusCode()).body(response);
    }

    /** 업로드한 파일이 허용 크기({@code max-file-size})를 넘었을 때 처리한다. */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException e) {

        CommonErrorCode errorCode = CommonErrorCode.FILE_SIZE_EXCEEDED;
        ErrorResponse response = new ErrorResponse(errorCode.getCode(), errorCode.getMessage());

        return ResponseEntity.status(errorCode.getStatusCode()).body(response);
    }

    /**
     * 위 핸들러에서 처리하지 못한 나머지 모든 예외를 처리한다.
     *
     * <p>내부 정보가 노출되지 않도록 응답에는 고정된 메시지만 담고, 상세 내용은 로그로만 남긴다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {

        CommonErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
        ErrorResponse response = new ErrorResponse(errorCode.getCode(), errorCode.getMessage());

        return ResponseEntity.status(errorCode.getStatusCode()).body(response);
    }
}
