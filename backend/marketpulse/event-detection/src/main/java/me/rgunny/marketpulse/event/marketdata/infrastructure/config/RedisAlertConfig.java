package me.rgunny.marketpulse.event.marketdata.infrastructure.config;

import me.rgunny.marketpulse.event.marketdata.domain.model.AlertHistory;
import me.rgunny.marketpulse.common.infrastructure.config.redis.RedisTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;

/**
 * AlertHistory용 Redis 설정
 * 
 * AlertHistory 객체를 JSON으로 직렬화하여 Redis에 저장
 * Common 모듈의 RedisTemplateBuilder를 활용하여 일관된 설정 적용
 */
@Configuration
public class RedisAlertConfig {
    
    /**
     * AlertHistory 전용 ReactiveRedisTemplate
     * 
     * AlertHistory는 Record 기반 불변 객체이므로 정적 타입 직렬화 사용
     * Jackson2JsonRedisSerializer로 타입 안전성 보장
     * 
     * @param connectionFactory Redis 연결 팩토리
     * @return ReactiveRedisTemplate<String, AlertHistory>
     */
    @Bean
    public ReactiveRedisTemplate<String, AlertHistory> alertHistoryRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {
        return RedisTemplateBuilder.jsonTemplate(connectionFactory, AlertHistory.class);
    }
}