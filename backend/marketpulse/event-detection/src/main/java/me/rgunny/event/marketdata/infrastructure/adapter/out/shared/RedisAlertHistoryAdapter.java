package me.rgunny.event.marketdata.infrastructure.adapter.out.shared;

import lombok.extern.slf4j.Slf4j;
import me.rgunny.event.marketdata.application.port.out.AlertHistoryPort;
import me.rgunny.event.marketdata.domain.model.AlertHistory;
import me.rgunny.event.marketdata.domain.model.AlertType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Redis 기반 알림 이력 관리 어댑터
 */
@Slf4j
@Component
public class RedisAlertHistoryAdapter implements AlertHistoryPort {
    
    private final ReactiveRedisTemplate<String, AlertHistory> redisTemplate;
    private final Clock clock;
    private static final String KEY_PREFIX = "alert:history:";
    
    public RedisAlertHistoryAdapter(
            @Qualifier("alertHistoryRedisTemplate") ReactiveRedisTemplate<String, AlertHistory> redisTemplate,
            Clock clock) {
        this.redisTemplate = redisTemplate;
        this.clock = clock;
    }
    
    @Override
    public Mono<AlertHistory> save(AlertHistory alertHistory) {
        String key = generateKey(alertHistory.symbol(), alertHistory.alertType());

        Duration ttl = Duration.between(Instant.now(clock), alertHistory.cooldownUntil());
        
        // TTL이 음수인 경우 방어 (이미 만료된 쿨다운)
        if (ttl.isNegative() || ttl.isZero()) {
            log.warn("Attempting to save expired alert history: {}, skipping", key);
            return Mono.just(alertHistory);
        }
        
        return redisTemplate.opsForValue()
                .set(key, alertHistory, ttl)
                .thenReturn(alertHistory)
                .doOnSuccess(saved -> log.debug("Saved alert history: {} with TTL: {}s", 
                        key, ttl.getSeconds()))
                .doOnError(error -> log.error("Failed to save alert history: {}", key, error));
    }
    
    @Override
    public Mono<AlertHistory> findBySymbolAndType(String symbol, AlertType alertType) {
        String key = generateKey(symbol, alertType);
        
        return redisTemplate.opsForValue()
                .get(key)
                .doOnNext(history -> log.debug("Found alert history: {}", key))
                .doOnError(error -> log.error("Failed to find alert history: {}", key, error))
                .onErrorResume(error -> Mono.empty());
    }
    
    @Override
    public Mono<Boolean> canSendAlert(String symbol, AlertType alertType) {
        return findBySymbolAndType(symbol, alertType)
                .map(history -> !history.isInCooldown(clock))
                .defaultIfEmpty(true)
                .doOnNext(canSend -> {
                    if (!canSend) {
                        log.info("Alert blocked by cooldown: {} - {}", symbol, alertType);
                    }
                });
    }
    
    @Override
    public Mono<Boolean> delete(String symbol, AlertType alertType) {
        String key = generateKey(symbol, alertType);
        
        return redisTemplate.opsForValue()
                .delete(key)
                .doOnSuccess(deleted -> {
                    if (deleted) {
                        log.info("Deleted alert history: {}", key);
                    }
                })
                .doOnError(error -> log.error("Failed to delete alert history: {}", key, error));
    }
    
    @Override
    public Mono<Long> deleteAllBySymbol(String symbol) {
        String pattern = KEY_PREFIX + symbol + ":*";
        
        return redisTemplate.keys(pattern)
                .flatMap(key -> redisTemplate.delete(key))
                .count()
                .doOnSuccess(count -> log.info("Deleted {} alert histories for symbol: {}", 
                        count, symbol))
                .doOnError(error -> log.error("Failed to delete alert histories for symbol: {}", 
                        symbol, error));
    }
    
    /**
     * Redis Key 생성
     * Format: alert:history:{symbol}:{alertType}
     * 
     * @param symbol 종목 코드
     * @param alertType 알림 타입
     * @return Redis Key
     */
    private String generateKey(String symbol, AlertType alertType) {
        return KEY_PREFIX + symbol + ":" + alertType.name();
    }
}