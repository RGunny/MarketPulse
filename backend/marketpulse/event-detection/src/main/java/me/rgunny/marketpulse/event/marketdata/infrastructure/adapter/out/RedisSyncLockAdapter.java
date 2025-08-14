package me.rgunny.marketpulse.event.marketdata.infrastructure.adapter.out;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.marketpulse.event.marketdata.application.port.out.SyncLockPort;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.UUID;

/**
 * Redis 기반 분산 락 구현
 * 멀티 인스턴스 환경에서 동시 실행 방지
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisSyncLockAdapter implements SyncLockPort {
    
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private static final String LOCK_PREFIX = "stock_master_sync:lock:";
    private final String instanceId = UUID.randomUUID().toString();
    
    @Override
    public Mono<Boolean> tryLock(String key, Duration ttl) {
        String lockKey = LOCK_PREFIX + key;
        
        return reactiveRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, instanceId, ttl)
                .doOnNext(locked -> {
                    if (locked) {
                        log.info("분산 락 획득 성공: key={}, instanceId={}, ttl={}", 
                                key, instanceId, ttl);
                    } else {
                        log.warn("분산 락 획득 실패: key={} (다른 인스턴스가 보유 중)", key);
                    }
                })
                .onErrorResume(error -> {
                    log.error("분산 락 획득 중 오류: key={}", key, error);
                    return Mono.just(false);
                });
    }
    
    @Override
    public Mono<Void> unlock(String key) {
        String lockKey = LOCK_PREFIX + key;
        
        // 자신이 획득한 락만 해제 (CAS 방식)
        return reactiveRedisTemplate.opsForValue()
                .get(lockKey)
                .filter(instanceId::equals)
                .flatMap(value -> reactiveRedisTemplate.delete(lockKey))
                .doOnNext(deleted -> {
                    if (deleted > 0) {
                        log.info("분산 락 해제 성공: key={}, instanceId={}", key, instanceId);
                    }
                })
                .then()
                .onErrorResume(error -> {
                    log.error("분산 락 해제 중 오류: key={}", key, error);
                    return Mono.empty();
                });
    }
    
    @Override
    public Mono<Boolean> isLocked(String key) {
        String lockKey = LOCK_PREFIX + key;
        
        return reactiveRedisTemplate.hasKey(lockKey)
                .doOnNext(locked -> log.debug("분산 락 상태 조회: key={}, locked={}", key, locked))
                .onErrorReturn(false);
    }
    
    @Override
    public Mono<Boolean> renewLock(String key, Duration ttl) {
        String lockKey = LOCK_PREFIX + key;
        
        // 자신이 보유한 락만 갱신
        return reactiveRedisTemplate.opsForValue()
                .get(lockKey)
                .filter(instanceId::equals)
                .flatMap(value -> reactiveRedisTemplate.expire(lockKey, ttl))
                .doOnNext(renewed -> {
                    if (renewed) {
                        log.info("분산 락 갱신 성공: key={}, ttl={}", key, ttl);
                    } else {
                        log.warn("분산 락 갱신 실패: key={} (락을 보유하지 않음)", key);
                    }
                })
                .onErrorReturn(false);
    }
}