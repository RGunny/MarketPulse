package me.rgunny.event.marketdata.infrastructure.adapter.in.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.marketpulse.common.core.response.Result;
import me.rgunny.event.marketdata.domain.error.StockPriceErrorCode;
import me.rgunny.event.marketdata.infrastructure.resilience.KISApiCircuitBreakerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * 서킷브레이커 모니터링 및 관리 API
 * 
 * 운영 중 서킷브레이커 상태 확인 및 수동 제어
 * Actuator와 별도로 비즈니스 관점의 모니터링 제공
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/circuit-breaker")
@RequiredArgsConstructor
public class CircuitBreakerMonitorController {
    
    private final KISApiCircuitBreakerService circuitBreakerService;
    
    /**
     * 서킷브레이커 상태 조회
     * 
     * @return 현재 상태 (CLOSED, OPEN, HALF_OPEN, DISABLED, FORCED_OPEN)
     */
    @GetMapping("/status")
    public Mono<ResponseEntity<Result<CircuitBreakerStatus>>> getStatus() {
        return Mono.fromCallable(() -> {
            var state = circuitBreakerService.getCircuitBreakerState();
            var metrics = circuitBreakerService.getMetrics();
            
            var status = new CircuitBreakerStatus(
                    metrics.name(),
                    state,
                    metrics.getHealthStatus(),
                    metrics.failureRate(),
                    metrics.slowCallRate(),
                    metrics.totalCalls(),
                    metrics.failedCalls(),
                    metrics.successfulCalls(),
                    getRecommendation(state, metrics.failureRate())
            );
            
            return ResponseEntity.ok(Result.success(status));
        });
    }
    
    /**
     * 서킷브레이커 메트릭 상세 조회
     * 
     * @return 실패율, 느린 호출 비율, 호출 통계 등
     */
    @GetMapping("/metrics")
    public Mono<ResponseEntity<Result<KISApiCircuitBreakerService.CircuitBreakerMetrics>>> getMetrics() {
        return Mono.fromCallable(() -> {
            var metrics = circuitBreakerService.getMetrics();
            return ResponseEntity.ok(Result.success(metrics));
        });
    }
    
    /**
     * 서킷브레이커 수동 리셋
     * 
     * 장애 복구 확인 후 수동으로 리셋
     * 주의: 관리자 권한 필요 (향후 Spring Security 적용)
     */
    @PostMapping("/reset")
    public Mono<ResponseEntity<Result<String>>> resetCircuitBreaker() {
        log.warn("Circuit breaker manual reset requested");
        
        return circuitBreakerService.resetCircuitBreaker()
                .then(Mono.fromCallable(() -> {
                    log.info("Circuit breaker has been reset successfully");
                    return ResponseEntity.ok(Result.success("Circuit breaker reset successfully"));
                }))
                .onErrorResume(error -> {
                    log.error("Failed to reset circuit breaker", error);
                    return Mono.just(ResponseEntity.internalServerError()
                            .body(Result.failure(StockPriceErrorCode.STOCK_PRICE_005)));
                });
    }
    
    /**
     * 서킷브레이커 강제 Open
     * 
     * 긴급 상황에서 API 호출 차단
     * 주의: 관리자 권한 필요
     */
    @PostMapping("/force-open")
    public Mono<ResponseEntity<Result<String>>> forceOpenCircuitBreaker() {
        log.warn("Circuit breaker force open requested");
        
        return circuitBreakerService.forceOpenCircuitBreaker()
                .then(Mono.fromCallable(() -> {
                    log.warn("Circuit breaker has been forced open");
                    return ResponseEntity.ok(Result.success("Circuit breaker forced open successfully"));
                }))
                .onErrorResume(error -> {
                    log.error("Failed to force open circuit breaker", error);
                    return Mono.just(ResponseEntity.internalServerError()
                            .body(Result.failure(StockPriceErrorCode.STOCK_PRICE_005)));
                });
    }
    
    /**
     * 권장 조치 생성
     */
    private String getRecommendation(String state, float failureRate) {
        return switch (state) {
            case "OPEN" -> String.format(
                    "서킷 차단 중. 실패율: %.1f%%. KIS API 서버 상태 확인 필요. 30초 후 자동 복구 시도.",
                    failureRate
            );
            case "HALF_OPEN" -> "복구 테스트 중. 5개 호출로 안정성 검증 진행.";
            case "CLOSED" -> failureRate > 20 
                    ? String.format("정상 작동 중이나 실패율(%.1f%%) 상승 추세. 모니터링 강화 필요.", failureRate)
                    : "정상 작동 중";
            case "FORCED_OPEN" -> "수동 차단 상태. 문제 해결 후 수동 리셋 필요.";
            default -> "알 수 없는 상태";
        };
    }
    
    /**
     * 서킷브레이커 상태 DTO
     */
    public record CircuitBreakerStatus(
            String name,
            String state,
            String healthStatus,
            float failureRate,
            float slowCallRate,
            int totalCalls,
            int failedCalls,
            int successfulCalls,
            String recommendation
    ) {
        /**
         * 대시보드용 간단 상태
         */
        public String getSimpleStatus() {
            return healthStatus + " (" + state + ")";
        }
        
        /**
         * 알림 필요 여부
         */
        public boolean needsAlert() {
            return "OPEN".equals(state) || "FORCED_OPEN".equals(state) || failureRate > 40;
        }
    }
}