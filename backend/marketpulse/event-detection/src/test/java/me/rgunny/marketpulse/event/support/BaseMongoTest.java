package me.rgunny.marketpulse.event.support;

import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * MongoDB 테스트 베이스 클래스 - Local MongoDB 전용
 * 
 * Testcontainers 사용 시: MongoTestcontainersTest 상속
 * Local MongoDB 사용 시: 이 클래스 상속
 */
@DataMongoTest
@ActiveProfiles("test")
public abstract class BaseMongoTest {
    // Local MongoDB 사용 - application-test.yml 설정 사용
    // spring.data.mongodb 설정이 그대로 적용됨
}