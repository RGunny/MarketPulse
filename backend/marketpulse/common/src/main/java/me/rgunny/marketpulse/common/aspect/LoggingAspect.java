package me.rgunny.marketpulse.common.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Aspect
@Component
@Slf4j
public class ApiLoggingAspect {

    @Pointcut("execution(* me.rgunny.marketpulse..*Controller.*(..))")
    public void controllerPointcut() {}

    @Around("controllerPointcut()")
    public Object logApiExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String traceId = generateTraceId();
        HttpServletRequest request = getCurrentRequest();

        // MDC에 추적 정보 설정
        MDC.put("traceId", traceId);

        String apiInfo = joinPoint.getTarget().getClass().getSimpleName() + "." + joinPoint.getSignature().getName();
        String httpInfo = getHttpInfo(request);
        long startTime = System.currentTimeMillis();

        try {
            // API 시작 로그
            log.info("API_START [{}] {}", httpInfo, apiInfo);

            Object result = joinPoint.proceed();

            // API 성공 로그
            long executionTime = System.currentTimeMillis() - startTime;
            log.info("API_SUCCESS [{}] {} ({}ms)", httpInfo, apiInfo, executionTime);

            return result;

        } catch (Exception e) {
            // API 실패 로그
            long executionTime = System.currentTimeMillis() - startTime;

            if (isBusinessException(e)) {
                log.warn("API_BUSINESS_ERROR [{}] {} ({}ms) - {}", httpInfo, apiInfo, executionTime, e.getMessage());
            } else {
                log.error("API_SYSTEM_ERROR [{}] {} ({}ms)", httpInfo, apiInfo, executionTime, e);
            }

            throw e;

        } finally {
            MDC.clear();
        }
    }

    // ===== 핵심 헬퍼 메서드만 =====

    private String generateTraceId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        } catch (Exception e) {
            return null;
        }
    }

    private String getHttpInfo(HttpServletRequest request) {
        if (request == null) {
            return "UNKNOWN";
        }
        return request.getMethod() + " " + request.getRequestURI();
    }

    private boolean isBusinessException(Exception e) {
        return e.getClass().getSimpleName().contains("Business");
    }
}