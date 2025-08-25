package me.rgunny.marketpulse.messaging.kafka.fixture;

import me.rgunny.marketpulse.messaging.kafka.core.KafkaEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * 테스트용 이벤트 Fixture
 * 라이브러리 테스트 시 도메인 의존성 없이 사용
 */
public record TestEvent(
        String id,
        String aggregateId,
        String message,
        Instant timestamp,
        TestEventType type
) implements KafkaEvent {
    
    public enum TestEventType {
        TEST_CREATED,
        TEST_UPDATED,
        TEST_DELETED
    }
    
    @Override
    public String getId() {
        return id;
    }
    
    @Override
    public String getSource() {
        return "test.messaging.kafka";
    }
    
    @Override
    public String getType() {
        return switch (type) {
            case TEST_CREATED -> "test.created";
            case TEST_UPDATED -> "test.updated";
            case TEST_DELETED -> "test.deleted";
        };
    }
    
    @Override
    public Instant getTime() {
        return timestamp;
    }
    
    @Override
    public String getAggregateId() {
        return aggregateId;
    }
    
    /**
     * 기본 테스트 이벤트 생성
     */
    public static TestEvent sample() {
        return new TestEvent(
                UUID.randomUUID().toString(),
                "TEST-001",
                "Test message",
                Instant.now(),
                TestEventType.TEST_CREATED
        );
    }
    
    /**
     * 커스텀 메시지로 테스트 이벤트 생성
     */
    public static TestEvent withMessage(String message) {
        return new TestEvent(
                UUID.randomUUID().toString(),
                "TEST-001",
                message,
                Instant.now(),
                TestEventType.TEST_CREATED
        );
    }
    
    /**
     * 특정 타입의 테스트 이벤트 생성
     */
    public static TestEvent withType(TestEventType type) {
        return new TestEvent(
                UUID.randomUUID().toString(),
                "TEST-001",
                "Test " + type.name(),
                Instant.now(),
                type
        );
    }
    
    /**
     * Builder 패턴 구현
     */
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String id;
        private String aggregateId;
        private String message;
        private Instant timestamp;
        private TestEventType type;
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder aggregateId(String aggregateId) {
            this.aggregateId = aggregateId;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public Builder timestamp(Instant timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder type(TestEventType type) {
            this.type = type;
            return this;
        }
        
        public TestEvent build() {
            return new TestEvent(id, aggregateId, message, timestamp, type);
        }
    }
}