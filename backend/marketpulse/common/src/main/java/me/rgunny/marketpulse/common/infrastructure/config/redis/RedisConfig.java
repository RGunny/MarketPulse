package me.rgunny.marketpulse.common.infrastructure.config.redis;

import me.rgunny.marketpulse.common.infrastructure.config.redis.RedisTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

/**
 * Redis 공통 설정
 * 
 * 각 모듈에서 필요한 기본 Redis 설정 제공
 */
@Configuration
public class RedisConfig {

    /**
     * String 타입 전용 ReactiveRedisTemplate
     * - KIS 토큰 캐싱용으로 최적화
     * - 간단한 키-값 캐싱에 사용
     */
    @Bean
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {
        return RedisTemplateBuilder.stringTemplate(connectionFactory);
    }
}
