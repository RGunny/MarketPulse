package me.rgunny.marketpulse.event.marketdata.infrastructure.adapter.in.init;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * 알림 이력 초기화
 * 
 * 앱 시작시 Redis에 저장된 알림 쿨다운 이력을 선택적으로 초기화
 */
@Slf4j
@Component
@ConditionalOnProperty(
    prefix = "marketpulse.alert.cleanup",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = false
)
public class AlertHistoryCleaner {
    
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    
    public AlertHistoryCleaner(@Qualifier("reactiveRedisTemplate") ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    private static final String ALERT_HISTORY_KEY_PATTERN = "alert:history:*";
    
    @PostConstruct
    public void cleanupAlertHistory() {
        log.info("Starting alert history cleanup on application startup");
        
        deleteAlertHistoryKeys()
            .doOnSuccess(count -> {
                if (count > 0) {
                    log.info("Successfully deleted {} alert history entries from Redis", count);
                } else {
                    log.info("No alert history entries found in Redis");
                }
            })
            .doOnError(error -> log.error("Failed to cleanup alert history: {}", error.getMessage()))
            .subscribe();
    }
    
    /**
     * Redis에서 알림 이력 키 삭제
     */
    private Mono<Long> deleteAlertHistoryKeys() {
        return redisTemplate.keys(ALERT_HISTORY_KEY_PATTERN)
            .collectList()
            .flatMap(keys -> {
                if (keys.isEmpty()) {
                    return Mono.just(0L);
                }
                log.debug("Found {} alert history keys to delete", keys.size());
                return redisTemplate.delete(keys.toArray(new String[0]));
            });
    }
    
    /**
     * 수동으로 알림 이력 초기화 (관리 API용)
     */
    public Mono<Long> cleanupManually() {
        log.info("Manual alert history cleanup requested");
        return deleteAlertHistoryKeys()
            .doOnSuccess(count -> log.info("Manually deleted {} alert history entries", count));
    }
}