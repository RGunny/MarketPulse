package me.rgunny.marketpulse.event.marketdata.infrastructure.adapter.out.kis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * KIS API 재시도 서비스
 * 
 * 네트워크 오류 및 일시적 장애에 대한 재시도 로직 제공
 */
@Slf4j
@Service
public class KISApiRetryService {
    
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final Duration MIN_BACKOFF = Duration.ofMillis(500);
    private static final Duration MAX_BACKOFF = Duration.ofSeconds(5);
    
    /**
     * 지수 백오프를 사용한 재시도 전략
     */
    public <T> Mono<T> withRetry(Mono<T> source, String operation) {
        AtomicInteger attemptCounter = new AtomicInteger(0);
        
        return source
                .doOnSubscribe(s -> {
                    attemptCounter.set(0);
                    log.debug("Starting operation: {}", operation);
                })
                .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, MIN_BACKOFF)
                        .maxBackoff(MAX_BACKOFF)
                        .jitter(0.5)  // 지터 추가로 재시도 분산
                        .filter(throwable -> isRetryableException(throwable))
                        .doBeforeRetry(signal -> {
                            int attempt = attemptCounter.incrementAndGet();
                            log.warn("Retry attempt {} for operation '{}' due to: {}", 
                                    attempt, operation, signal.failure().getMessage());
                        })
                        .onRetryExhaustedThrow((spec, signal) -> {
                            log.error("All retry attempts exhausted for operation '{}' after {} attempts", 
                                    operation, attemptCounter.get());
                            return signal.failure();
                        }))
                .doOnError(error -> {
                    if (!isRetryableException(error)) {
                        log.error("Non-retryable error for operation '{}': {}", 
                                operation, error.getMessage());
                    }
                })
                .doOnSuccess(result -> {
                    if (attemptCounter.get() > 0) {
                        log.info("Operation '{}' succeeded after {} retries", 
                                operation, attemptCounter.get());
                    }
                });
    }
    
    /**
     * 재시도 가능한 예외 판별
     */
    private boolean isRetryableException(Throwable throwable) {
        // 네트워크 관련 예외들은 재시도
        if (throwable instanceof java.io.IOException || throwable instanceof java.util.concurrent.TimeoutException) {
            return true;
        }
        
        // WebClient 예외 중 5xx 에러는 재시도
        if (throwable instanceof org.springframework.web.reactive.function.client.WebClientResponseException responseException) {
            int statusCode = responseException.getStatusCode().value();
            return statusCode >= 500 && statusCode < 600;  // 서버 에러만 재시도
        }
        
        // WebClient 요청 예외는 재시도
        if (throwable instanceof org.springframework.web.reactive.function.client.WebClientRequestException) {
            return true;
        }
        
        // 그 외 예외는 재시도하지 않음
        return false;
    }
    
    /**
     * 빠른 재시도 (작은 지연시간)
     */
    public <T> Mono<T> withQuickRetry(Mono<T> source, String operation) {
        return source
                .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(200))
                        .filter(throwable -> isRetryableException(throwable))
                        .doBeforeRetry(signal -> log.debug("Quick retry for '{}': {}", operation, signal.failure().getMessage())));
    }
}