package me.rgunny.marketpulse.event.marketdata.application.port.out;

import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * 동기화 락 관리 포트
 * 분산 환경에서 동시 실행 방지를 위한 락 추상화
 */
public interface SyncLockPort {
    
    /**
     * 락 획득 시도
     * 
     * @param key 락 키
     * @param ttl 락 유지 시간
     * @return 락 획득 성공 여부
     */
    Mono<Boolean> tryLock(String key, Duration ttl);
    
    /**
     * 락 해제
     * 
     * @param key 락 키
     * @return 완료 신호
     */
    Mono<Void> unlock(String key);
    
    /**
     * 락 상태 확인
     * 
     * @param key 락 키
     * @return 락 보유 여부
     */
    Mono<Boolean> isLocked(String key);
    
    /**
     * 락 갱신 (TTL 연장)
     * 
     * @param key 락 키
     * @param ttl 새로운 TTL
     * @return 갱신 성공 여부
     */
    Mono<Boolean> renewLock(String key, Duration ttl);
}