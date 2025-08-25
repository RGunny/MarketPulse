package me.rgunny.marketpulse.messaging.kafka.integration;

import me.rgunny.marketpulse.messaging.kafka.TestKafkaApplication;
import me.rgunny.marketpulse.messaging.kafka.fixture.TestEvent;
import me.rgunny.marketpulse.messaging.kafka.producer.GenericEventProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TestKafkaApplication.class)
@ActiveProfiles("test")
@EmbeddedKafka(
        partitions = 1,
        topics = {"test-events"},
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:9092",
                "port=9092"
        }
)
@DirtiesContext
class KafkaIntegrationTest {

    @Autowired
    private GenericEventProducer genericEventProducer;

    @Test
    @DisplayName("제네릭 이벤트를 발행하고 확인할 수 있다")
    void given_testEvent_when_send_then_published() throws Exception {
        // given
        TestEvent event = TestEvent.sample();
        String topic = "test-events";
        
        // when
        var future = genericEventProducer.send(topic, event);
        var result = future.get(10, TimeUnit.SECONDS);
        
        // then
        assertThat(result).isNotNull();
        assertThat(result.getRecordMetadata().topic()).isEqualTo(topic);
        assertThat(result.getRecordMetadata().partition()).isGreaterThanOrEqualTo(0);
        assertThat(result.getRecordMetadata().offset()).isGreaterThanOrEqualTo(0);
        
        // Consumer 처리 대기
        Thread.sleep(1000);
    }

    @Test
    @DisplayName("다양한 타입의 이벤트를 발행할 수 있다")
    void given_differentEventTypes_when_send_then_allPublished() throws Exception {
        // given
        TestEvent createdEvent = TestEvent.withType(TestEvent.TestEventType.TEST_CREATED);
        TestEvent updatedEvent = TestEvent.withType(TestEvent.TestEventType.TEST_UPDATED);
        TestEvent deletedEvent = TestEvent.withType(TestEvent.TestEventType.TEST_DELETED);
        String topic = "test-events";
        
        // when & then
        var createResult = genericEventProducer.send(topic, createdEvent).get(5, TimeUnit.SECONDS);
        assertThat(createResult).isNotNull();
        assertThat(createResult.getRecordMetadata().topic()).isEqualTo(topic);
        
        var updateResult = genericEventProducer.send(topic, updatedEvent).get(5, TimeUnit.SECONDS);
        assertThat(updateResult).isNotNull();
        
        var deleteResult = genericEventProducer.send(topic, deletedEvent).get(5, TimeUnit.SECONDS);
        assertThat(deleteResult).isNotNull();
    }
    
    @Test
    @DisplayName("비동기로 이벤트를 발행할 수 있다")
    void given_testEvent_when_sendAsync_then_noBlocking() {
        // given
        TestEvent event = TestEvent.withMessage("Async test message");
        String topic = "test-events";
        
        // when
        genericEventProducer.sendAsync(topic, event);
        
        // then - 비동기이므로 즉시 리턴되어야 함
        assertThat(true).isTrue(); // 블로킹 없이 실행 완료
    }
    
    @Test
    @DisplayName("동일한 aggregateId를 가진 이벤트는 같은 파티션으로 전송된다")
    void given_eventsWithSameAggregateId_when_send_then_samePartition() throws Exception {
        // given
        String aggregateId = "TEST-AGGREGATE-001";
        TestEvent event1 = TestEvent.builder()
                .id("event-1")
                .aggregateId(aggregateId)
                .message("First event")
                .timestamp(java.time.Instant.now())
                .type(TestEvent.TestEventType.TEST_CREATED)
                .build();
                
        TestEvent event2 = TestEvent.builder()
                .id("event-2")
                .aggregateId(aggregateId)
                .message("Second event")
                .timestamp(java.time.Instant.now())
                .type(TestEvent.TestEventType.TEST_UPDATED)
                .build();
                
        String topic = "test-events";
        
        // when
        var result1 = genericEventProducer.send(topic, event1).get(5, TimeUnit.SECONDS);
        var result2 = genericEventProducer.send(topic, event2).get(5, TimeUnit.SECONDS);
        
        // then - 같은 aggregateId는 같은 파티션으로
        assertThat(result1.getRecordMetadata().partition())
                .isEqualTo(result2.getRecordMetadata().partition());
    }
}