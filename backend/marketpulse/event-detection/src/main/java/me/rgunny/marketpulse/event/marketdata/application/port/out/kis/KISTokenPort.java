package me.rgunny.marketpulse.event.marketdata.application.port.out.kis;

import reactor.core.publisher.Mono;

/**
 * KIS API 토큰 관리 포트
 * 
 * 토큰 발급, 캐싱, 갱신을 담당하는 추상화
 * 헥사고날 아키텍처: Infrastructure 간 직접 의존을 방지
 */
public interface KISTokenPort {
    
    /**
     * 액세스 토큰 획득
     * 캐시 우선 조회 후 없으면 신규 발급
     * 
     * @return 액세스 토큰
     */
    Mono<String> getAccessToken();
    
    /**
     * 토큰 강제 갱신
     * 캐시를 무시하고 새 토큰 발급
     * 
     * @return 새로운 액세스 토큰
     */
    Mono<String> refreshToken();
    
    /**
     * 토큰 유효성 검사
     * 
     * @return 토큰 유효 여부
     */
    Mono<Boolean> isTokenValid();
}