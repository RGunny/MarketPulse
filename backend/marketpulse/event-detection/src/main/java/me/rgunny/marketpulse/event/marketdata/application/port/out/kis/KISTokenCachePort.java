package me.rgunny.marketpulse.event.marketdata.application.port.out.kis;

import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * KIS OAuth 토큰 캐시 관리 포트
 */
public interface KISTokenCachePort {

    /**
     * 캐시된 토큰 조회
     * @return 토큰 (없으면 empty)
     */
    Mono<String> getToken();

    /**
     * 토큰 저장
     * @param token 저장할 토큰
     * @param ttl 만료 시간
     * @return 저장 완료 신호
     */
    Mono<Void> saveToken(String token, Duration ttl);

    /**
     * 토큰 유효성 확인 (존재 여부)
     * @return 토큰 존재 여부
     */
    Mono<Boolean> isTokenValid();

    /**
     * 토큰 삭제
     * @return 삭제 완료 신호
     */
    Mono<Void> clearToken();

    /**
     * 토큰 만료시간 조회
     * @return 만료까지 남은 시간 (seconds)
     */
    Mono<Long> getTokenTtl();
}