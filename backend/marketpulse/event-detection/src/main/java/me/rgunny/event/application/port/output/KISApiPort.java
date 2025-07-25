package me.rgunny.event.application.port.output;

import reactor.core.publisher.Mono;

public interface KISApiPort {
    
    /**
     * 새로운 액세스 토큰 발급 (항상 API 호출)
     * @return 새 액세스 토큰
     */
    Mono<String> getAccessToken();
    
    /**
     * 캐시된 토큰 또는 새 토큰 반환 (캐시 우선)
     * @return 유효한 액세스 토큰
     */
    Mono<String> getCachedOrNewToken();
    
    /**
     * KIS API 연결 상태 검증
     * @return 연결 성공 여부
     */
    Mono<Boolean> validateConnection();
}
