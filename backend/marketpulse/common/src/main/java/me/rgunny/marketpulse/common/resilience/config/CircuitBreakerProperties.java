package me.rgunny.marketpulse.common.resilience.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 서킷브레이커 공통 설정
 * 
 * - 빠른 장애 감지와 복구
 * - 실시간 모니터링 지원
 * - 동적 설정 변경 가능
 */
@ConfigurationProperties(prefix = "resilience4j.circuitbreaker")
public record CircuitBreakerProperties(
        Integer failureRateThreshold,           // 실패율 임계치 (기본 50%)
        Integer slowCallRateThreshold,          // 느린 호출 비율 임계치 (기본 100%)
        Duration slowCallDurationThreshold,     // 느린 호출 기준 시간 (기본 60초)
        Integer permittedNumberOfCallsInHalfOpenState,  // Half-Open 상태에서 허용 호출 수 (기본 10)
        Integer slidingWindowSize,              // 슬라이딩 윈도우 크기 (기본 100)
        String slidingWindowType,               // COUNT_BASED or TIME_BASED (기본 COUNT_BASED)
        Integer minimumNumberOfCalls,           // 최소 호출 수 (기본 10)
        Duration waitDurationInOpenState,       // Open 상태 대기 시간 (기본 60초)
        Boolean automaticTransitionFromOpenToHalfOpenEnabled,  // 자동 전환 활성화 (기본 false)
        Boolean recordExceptions,               // 예외 기록 여부 (기본 true)
        Boolean registerHealthIndicator         // Health Indicator 등록 (기본 true)
) {
    
    /**
     * 기본값을 적용한 빌더
     */
    public static CircuitBreakerProperties withDefaults() {
        return new CircuitBreakerProperties(
                50,                               // 50% 실패율에서 Open
                100,                              // 100% 느린 호출에서 Open
                Duration.ofSeconds(60),           // 60초 이상이면 느린 호출
                10,                               // Half-Open에서 10개 테스트
                100,                              // 최근 100개 호출 기준
                "COUNT_BASED",                    // 횟수 기반 윈도우
                10,                               // 최소 10개 호출 후 평가
                Duration.ofSeconds(60),           // Open 상태 60초 유지
                false,                            // 수동 전환
                true,                             // 예외 기록
                true                              // Health 체크 등록
        );
    }
    
    /**
     * KIS API용 커스텀 설정
     * - 외부 API 특성상 네트워크 지연 고려
     * - 빠른 복구 시도
     */
    public static CircuitBreakerProperties forExternalApi() {
        return new CircuitBreakerProperties(
                30,                               // 30% 실패율 (더 민감하게)
                50,                               // 50% 느린 호출
                Duration.ofSeconds(10),           // 10초 이상이면 느린 호출
                5,                                // Half-Open에서 5개만 테스트
                50,                               // 최근 50개 호출 기준
                "COUNT_BASED",
                5,                                // 최소 5개 호출 후 평가
                Duration.ofSeconds(30),           // Open 상태 30초 유지 (빠른 복구)
                true,                             // 자동 전환 활성화
                true,
                true
        );
    }
}