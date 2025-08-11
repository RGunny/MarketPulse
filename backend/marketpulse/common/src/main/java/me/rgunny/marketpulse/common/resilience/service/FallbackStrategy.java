package me.rgunny.marketpulse.common.resilience.service;

import reactor.core.publisher.Mono;

/**
 * Fallback 전략 인터페이스
 * 
 * 서킷브레이커 Open 상태에서 실행할 대체 로직 정의
 * 다양한 전략 패턴 구현 가능:
 * - 캐시 기반 Fallback
 * - 기본값 반환
 * - 다른 서비스 호출
 * - 재시도 큐잉
 */
@FunctionalInterface
public interface FallbackStrategy<T> {
    
    /**
     * Fallback 로직 실행
     * 
     * @param throwable 원본 예외
     * @param context Fallback 실행 컨텍스트 (선택적 파라미터)
     * @return Fallback 결과
     */
    Mono<T> execute(Throwable throwable, Object... context);
    
    /**
     * 캐시 기반 Fallback 전략
     */
    static <T> FallbackStrategy<T> fromCache(CacheProvider<T> cacheProvider, String key) {
        return (throwable, context) -> {
            return cacheProvider.get(key)
                    .doOnSubscribe(s -> logFallback("CACHE", key, throwable))
                    .switchIfEmpty(Mono.error(new FallbackException("Cache miss for key: " + key, throwable)));
        };
    }
    
    /**
     * 기본값 반환 전략
     */
    static <T> FallbackStrategy<T> withDefault(T defaultValue) {
        return (throwable, context) -> {
            logFallback("DEFAULT", defaultValue.toString(), throwable);
            return Mono.just(defaultValue);
        };
    }
    
    /**
     * 예외 전파 전략 (Fallback 없음)
     */
    static <T> FallbackStrategy<T> propagateError() {
        return (throwable, context) -> {
            logFallback("PROPAGATE", "Error propagated", throwable);
            return Mono.error(throwable);
        };
    }
    
    /**
     * 복합 전략 - 여러 전략을 순차적으로 시도
     */
    static <T> FallbackStrategy<T> chain(FallbackStrategy<T>... strategies) {
        return (throwable, context) -> {
            Mono<T> result = Mono.error(throwable);
            for (FallbackStrategy<T> strategy : strategies) {
                result = result.onErrorResume(t -> strategy.execute(t, context));
            }
            return result;
        };
    }
    
    /**
     * Fallback 실행 로깅
     */
    private static void logFallback(String strategy, String details, Throwable throwable) {
        // 실제 구현에서는 SLF4J 사용
        System.out.printf("[FALLBACK] Strategy: %s, Details: %s, Cause: %s%n", 
                strategy, details, throwable.getMessage());
    }
    
    /**
     * 캐시 제공자 인터페이스
     */
    @FunctionalInterface
    interface CacheProvider<T> {
        Mono<T> get(String key);
    }
    
    /**
     * Fallback 실행 실패 예외
     */
    class FallbackException extends RuntimeException {
        public FallbackException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}