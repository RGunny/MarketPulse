package me.rgunny.marketpulse.event.notification.infrastructure.adapter.out.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.marketpulse.event.notification.application.port.out.NotificationHistoryPort;
import me.rgunny.marketpulse.event.notification.domain.model.NotificationHistory;
import me.rgunny.marketpulse.notification.grpc.NotificationServiceProto.PriceAlertType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * 알림 이력 Redis 저장소 어댑터
 *
 * - TTL 기반 자동 만료: 쿨다운 기간 + 5분 버퍼
 * - 종목별/타입별 키 구조: notification:history:{symbol}:{alertType}
 */
@Slf4j
@Component
public class NotificationHistoryRedisAdapter implements NotificationHistoryPort {
    
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    public NotificationHistoryRedisAdapter(
            @Qualifier("reactiveRedisTemplate") ReactiveRedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }
    
    private static final String KEY_PREFIX = "notification:history:";
    
    @Override
    public Mono<NotificationHistory> save(NotificationHistory history) {
        String key = buildKey(history.symbol(), history.alertType());
        
        return Mono.fromCallable(() -> serialize(history))
            .flatMap(json -> redisTemplate.opsForValue()
                .set(key, json, history.calculateTTL())
                .thenReturn(history))
            .doOnSuccess(saved -> log.debug("Notification history saved: symbol={}, type={}, cooldown={}",
                saved.symbol(), saved.alertType(), saved.cooldownPeriod()))
            .doOnError(error -> log.error("Failed to save notification history: symbol={}, type={}",
                history.symbol(), history.alertType(), error));
    }
    
    @Override
    public Mono<NotificationHistory> findLatest(String symbol, PriceAlertType alertType) {
        String key = buildKey(symbol, alertType);
        
        return redisTemplate.opsForValue()
            .get(key)
            .map(this::deserialize)
            .doOnNext(history -> log.debug("Found notification history: symbol={}, type={}, cooldown remaining={}",
                history.symbol(), history.alertType(), history.getRemainingCooldown()))
            .switchIfEmpty(Mono.fromRunnable(() -> 
                log.debug("No notification history found: symbol={}, type={}", symbol, alertType)));
    }
    
    @Override
    public Mono<Boolean> isInCooldown(String symbol, PriceAlertType alertType) {
        return findLatest(symbol, alertType)
            .map(NotificationHistory::isInCooldown)
            .defaultIfEmpty(false)  // 이력이 없으면 쿨다운 없음
            .doOnNext(inCooldown -> {
                if (inCooldown) {
                    log.info("Alert is in cooldown: symbol={}, type={}", symbol, alertType);
                }
            });
    }
    
    @Override
    public Mono<Duration> getRemainingCooldown(String symbol, PriceAlertType alertType) {
        return findLatest(symbol, alertType)
            .map(NotificationHistory::getRemainingCooldown)
            .defaultIfEmpty(Duration.ZERO);
    }
    
    @Override
    public Mono<Void> delete(String symbol, PriceAlertType alertType) {
        String key = buildKey(symbol, alertType);
        
        return redisTemplate.delete(key)
            .doOnSuccess(count -> log.info("Deleted notification history: symbol={}, type={}, count={}",
                symbol, alertType, count))
            .then();
    }
    
    @Override
    public Mono<Void> deleteAll() {
        // 패턴 매칭으로 모든 알림 이력 삭제
        return redisTemplate.keys(KEY_PREFIX + "*")
            .flatMap(redisTemplate::delete)
            .doOnComplete(() -> log.warn("All notification histories deleted"))
            .then();
    }
    
    /**
     * Redis 키 생성
     */
    private String buildKey(String symbol, PriceAlertType alertType) {
        return String.format("%s%s:%s", KEY_PREFIX, symbol, alertType);
    }
    
    /**
     * 객체 직렬화
     */
    private String serialize(NotificationHistory history) {
        try {
            return objectMapper.writeValueAsString(history);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize NotificationHistory", e);
            throw new IllegalStateException("Serialization failed", e);
        }
    }
    
    /**
     * 객체 역직렬화
     */
    private NotificationHistory deserialize(String json) {
        try {
            return objectMapper.readValue(json, NotificationHistory.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize NotificationHistory: {}", json, e);
            throw new IllegalStateException("Deserialization failed", e);
        }
    }
}