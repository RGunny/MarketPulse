package me.rgunny.event.marketdata.infrastructure.adapter.out.kis;

import me.rgunny.event.marketdata.application.port.out.kis.KISTokenCachePort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Redis 기반 KIS 토큰 캐시 구현체
 */
@Component
public class RedisKISTokenCacheAdapter implements KISTokenCachePort {

    private static final String TOKEN_KEY = "kis:oauth:token";
    
    private final ReactiveRedisTemplate<String, String> redisTemplate;

    public RedisKISTokenCacheAdapter(@Qualifier("reactiveRedisTemplate") ReactiveRedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Mono<String> getToken() {
        return redisTemplate.opsForValue()
                .get(TOKEN_KEY);
    }

    @Override
    public Mono<Void> saveToken(String token, Duration ttl) {
        return redisTemplate.opsForValue()
                .set(TOKEN_KEY, token, ttl)
                .then();
    }

    @Override
    public Mono<Boolean> isTokenValid() {
        return redisTemplate.hasKey(TOKEN_KEY);
    }

    @Override
    public Mono<Void> clearToken() {
        return redisTemplate.delete(TOKEN_KEY)
                .then();
    }

    @Override
    public Mono<Long> getTokenTtl() {
        return redisTemplate.getExpire(TOKEN_KEY)
                .map(Duration::getSeconds);
    }
}