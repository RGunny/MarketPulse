package me.rgunny.marketpulse.messaging.kafka.core;

import java.time.Instant;

/**
 * Kafka 이벤트의 기본 인터페이스
 */
public interface KafkaEvent {
    
    /**
     * 이벤트 고유 식별자
     */
    String getId();
    
    /**
     * 이벤트 발생 출처 (예: marketpulse.event-detection)
     */
    String getSource();
    
    /**
     * 이벤트 타입 (예: stock.price.changed)
     */
    String getType();
    
    /**
     * 이벤트 발생 시간
     */
    Instant getTime();
    
    /**
     * 집계 루트 ID (Kafka 파티션 키로 사용)
     * 예: 종목코드, 사용자ID 등
     */
    String getAggregateId();
    
    /**
     * CloudEvents 스펙 버전
     */
    default String getSpecVersion() {
        return "1.0";
    }
    
    /**
     * 컨텐츠 타입
     */
    default String getContentType() {
        return "application/json";
    }
    
    /**
     * 이벤트 데이터 스키마 URI (선택)
     */
    default String getDataSchema() {
        return null;
    }
}