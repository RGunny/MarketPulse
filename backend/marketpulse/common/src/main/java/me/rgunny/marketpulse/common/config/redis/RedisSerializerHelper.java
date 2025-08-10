package me.rgunny.marketpulse.common.config.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 직렬화 헬퍼
 * 
 * 각 모듈에서 ReactiveRedisTemplate 생성 시 재사용할 수 있는 공통 직렬화 설정 제공
 */
public final class RedisSerializerHelper {
    
    private RedisSerializerHelper() {}
    
    /**
     * String 키 직렬화기 생성
     */
    public static StringRedisSerializer stringSerializer() {
        return new StringRedisSerializer();
    }
    
    /**
     * 정적 타입용 JSON 직렬화기 생성
     * 
     * @param targetClass 직렬화 대상 클래스
     * @param <T> 타입 파라미터
     * @return Jackson2JsonRedisSerializer
     */
    public static <T> Jackson2JsonRedisSerializer<T> jsonSerializer(Class<T> targetClass) {
        return new Jackson2JsonRedisSerializer<>(createObjectMapper(), targetClass);
    }
    
    /**
     * 다형성 지원 JSON 직렬화기 생성
     * 
     * 타입 정보를 JSON에 포함시켜 역직렬화 시 정확한 타입 복원
     * 보안을 위해 허용된 패키지만 역직렬화 가능
     * 
     * @param trustedPackages 신뢰할 수 있는 패키지들
     * @return GenericJackson2JsonRedisSerializer
     */
    public static GenericJackson2JsonRedisSerializer polymorphicJsonSerializer(String... trustedPackages) {
        ObjectMapper mapper = createPolymorphicObjectMapper(trustedPackages);
        return new GenericJackson2JsonRedisSerializer(mapper);
    }
    
    /**
     * 기본 ObjectMapper 생성
     */
    private static ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.findAndRegisterModules();
        return mapper;
    }
    
    /**
     * 다형성 지원 ObjectMapper 생성
     */
    private static ObjectMapper createPolymorphicObjectMapper(String... trustedPackages) {
        ObjectMapper mapper = createObjectMapper();
        
        // 타입 검증기 설정 - 지정된 패키지만 역직렬화 허용
        BasicPolymorphicTypeValidator validator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .allowIfSubTypeIsArray()
                .build();
        
        if (trustedPackages.length > 0) {
            BasicPolymorphicTypeValidator.Builder builder = BasicPolymorphicTypeValidator.builder();
            for (String pkg : trustedPackages) {
                builder.allowIfSubType(pkg);
            }
            validator = builder.build();
        }
        
        mapper.activateDefaultTyping(validator, ObjectMapper.DefaultTyping.NON_FINAL);
        return mapper;
    }
}