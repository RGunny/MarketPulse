package me.rgunny.marketpulse.event.support;

import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * MongoDB 테스트 베이스 클래스 - Testcontainers 전용
 */
@DataMongoTest
@ActiveProfiles("test")
@Testcontainers
public abstract class MongoTestcontainersTest {
    
    @Container
    static MongoDBContainer mongodb = new MongoDBContainer("mongo:7.0")
            .withExposedPorts(27017);
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Testcontainers가 생성한 MongoDB 연결 정보를 동적으로 설정
        registry.add("spring.data.mongodb.uri", mongodb::getReplicaSetUrl);
    }
}