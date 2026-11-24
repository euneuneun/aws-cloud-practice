package com.awscloudpractice.common.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
public class LoggingAspect {

    @Pointcut("execution(* com.awscloudpractice.member.controller.MemberController.*(..))")
    private void memberControllerMethod() {}

    @Pointcut("execution(* com.awscloudpractice.common.exception.GlobalExceptionHandler.*(..))")
    private void handlerMethods() {}

    // controller 전 예외가 발생한 요청은 출력되지 않는다.
    @Around("memberControllerMethod()")
    public Object memberControllerAroundMethod(ProceedingJoinPoint joinPoint) throws Throwable {

        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        String method = request.getMethod();
        String url = request.getRequestURI();

        log.info("[API - AOP - LOG] AOP Before Method: {}, URL: {}", method, url);

        try {
            Object result = joinPoint.proceed(); // 진짜 컨트롤러 메서드 실행
            log.info("[API - AOP - LOG] AOP AfterReturning Method: {}, URL: {}", method, url);
            return result; // AOP 메서드가 반환한 값이 곧 컨트롤러의 반환값. return 안 하면 빈 응답
        } catch (Throwable exception) {
            // 스택 트레이스는 handlerMethod에서 출력 (중복 방지)
            log.error("[API - AOP - LOG] AOP ExceptionMethod: {}, URL: {}", method, url);
            throw exception; // GlobalExceptionHandler로 던짐
        } finally {
            log.info("[API - AOP - LOG] AOP After Method: {}, URL: {}", method, url);
        }
    }

    // 모든 에러 로그가 다 출력됨. (핸들러가 받는 예외를 같이 받아 스택 트레이스까지 출력)
    @Before("handlerMethods() && args(exception)")
    public void handlerMethod(Exception exception) {

        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        String method = request.getMethod();
        String url = request.getRequestURI();

        log.error(
                "[API - AOP - LOG] AOP ExceptionHandler Method: {}, URL: {}",
                method,
                url,
                exception);
    }
}
