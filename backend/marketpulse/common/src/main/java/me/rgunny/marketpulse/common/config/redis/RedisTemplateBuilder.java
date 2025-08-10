package me.rgunny.marketpulse.common.config.redis;

import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * ReactiveRedisTemplate 빌더
 * 
 * 각 모듈에서 쉽게 ReactiveRedisTemplate을 생성할 수 있도록 지원
 */
public final class RedisTemplateBuilder {
    
    private RedisTemplateBuilder() {
        // 유틸리티 클래스
    }
    
    /**
     * String-String 타입 ReactiveRedisTemplate 생성
     * 
     * @param connectionFactory Redis 연결 팩토리
     * @return ReactiveRedisTemplate<String, String>
     */
    public static ReactiveRedisTemplate<String, String> stringTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {
        
        RedisSerializer<String> serializer = RedisSerializerHelper.stringSerializer();
        
        RedisSerializationContext<String, String> context = RedisSerializationContext
                .<String, String>newSerializationContext()
                .key(serializer)
                .value(serializer)
                .hashKey(serializer)
                .hashValue(serializer)
                .build();
                
        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }
    
    /**
     * String 키와 JSON 값 타입 ReactiveRedisTemplate 생성
     * 
     * @param connectionFactory Redis 연결 팩토리
     * @param valueClass 값 타입 클래스
     * @param <T> 값 타입
     * @return ReactiveRedisTemplate<String, T>
     */
    public static <T> ReactiveRedisTemplate<String, T> jsonTemplate(
            ReactiveRedisConnectionFactory connectionFactory,
            Class<T> valueClass) {
        
        RedisSerializer<String> keySerializer = RedisSerializerHelper.stringSerializer();
        RedisSerializer<T> valueSerializer = RedisSerializerHelper.jsonSerializer(valueClass);
        
        RedisSerializationContext<String, T> context = RedisSerializationContext
                .<String, T>newSerializationContext()
                .key(keySerializer)
                .value(valueSerializer)
                .hashKey(keySerializer)
                .hashValue(valueSerializer)
                .build();
                
        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }
    
    /**
     * 다형성 지원 ReactiveRedisTemplate 생성
     * 
     * @param connectionFactory Redis 연결 팩토리
     * @param trustedPackages 신뢰할 수 있는 패키지들
     * @return ReactiveRedisTemplate<String, Object>
     */
    public static ReactiveRedisTemplate<String, Object> polymorphicTemplate(
            ReactiveRedisConnectionFactory connectionFactory,
            String... trustedPackages) {
        
        RedisSerializer<String> keySerializer = RedisSerializerHelper.stringSerializer();
        RedisSerializer<Object> valueSerializer = RedisSerializerHelper.polymorphicJsonSerializer(trustedPackages);
        
        RedisSerializationContext<String, Object> context = RedisSerializationContext
                .<String, Object>newSerializationContext()
                .key(keySerializer)
                .value(valueSerializer)
                .hashKey(keySerializer)
                .hashValue(valueSerializer)
                .build();
                
        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }
}