package me.rgunny.marketpulse.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 설정
 * - Spring Boot Auto-configuration을 활용하되 필요한 Bean만 추가 정의
 * - ReactiveRedisConnectionFactory는 Auto-configuration에 위임
 */
@Configuration
public class RedisConfig {

    /**
     * String 타입 전용 ReactiveRedisTemplate
     * - KIS 토큰 캐싱용으로 최적화
     */
    @Bean("reactiveRedisTemplate")
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {
        
        StringRedisSerializer serializer = new StringRedisSerializer();
        
        RedisSerializationContext<String, String> context = RedisSerializationContext
                .<String, String>newSerializationContext()
                .key(serializer)
                .value(serializer)
                .hashKey(serializer)
                .hashValue(serializer)
                .build();
                
        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }
}
