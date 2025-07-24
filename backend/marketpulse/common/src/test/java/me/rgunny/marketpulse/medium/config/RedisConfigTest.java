package me.rgunny.marketpulse.medium.config;

import me.rgunny.marketpulse.common.config.RedisConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

@SpringBootTest(classes = RedisConfig.class)
@Testcontainers
@DisplayName("RedisConfig - 실제 Redis 연동 테스트 (medium)")
class RedisConfigTest {

    @Container
    static final GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Test
    @DisplayName("실제 Redis 서버와 연결하여 데이터 저장 및 조회가 정상 동작한다")
    void givenRealRedisServer_whenStoreAndRetrieve_thenDataPersistedCorrectly() {
        // given
        String testKey = "medium:test:basic";
        String testValue = "Medium test with real Redis";

        // when
        Mono<String> result = reactiveRedisTemplate.opsForValue()
                .set(testKey, testValue)
                .then(reactiveRedisTemplate.opsForValue().get(testKey))
                .doFinally(signalType -> reactiveRedisTemplate.delete(testKey).subscribe());

        // then
        StepVerifier.create(result)
                .expectNext(testValue)
                .verifyComplete();
    }

    @Test
    @DisplayName("TTL 설정된 데이터가 지정된 시간 동안 유지된다")
    void givenDataWithTTL_whenSetWithExpiration_thenDataAvailableWithinTTL() {
        // given
        String testKey = "medium:test:ttl";
        String testValue = "TTL test value";
        Duration ttl = Duration.ofSeconds(3);

        // when
        Mono<String> result = reactiveRedisTemplate.opsForValue()
                .set(testKey, testValue, ttl)
                .then(reactiveRedisTemplate.opsForValue().get(testKey))
                .doFinally(signalType -> reactiveRedisTemplate.delete(testKey).subscribe());

        // then
        StepVerifier.create(result)
                .expectNext(testValue)
                .verifyComplete();
    }

    @Test
    @DisplayName("한글 및 특수문자 데이터가 정상적으로 직렬화/역직렬화된다")
    void givenComplexStringData_whenStoreAndRetrieve_thenSerializationWorksCorrectly() {
        // given
        String testKey = "medium:test:serialization";
        String complexValue = "복잡한 한글 데이터 & Special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?";

        // when
        Mono<String> result = reactiveRedisTemplate.opsForValue()
                .set(testKey, complexValue)
                .then(reactiveRedisTemplate.opsForValue().get(testKey))
                .doFinally(signalType -> reactiveRedisTemplate.delete(testKey).subscribe());

        // then
        StepVerifier.create(result)
                .expectNext(complexValue)
                .verifyComplete();
    }

    @Test
    @DisplayName("존재하지 않는 키 조회 시 빈 결과를 반환한다")
    void givenNonExistentKey_whenGet_thenReturnsEmpty() {
        // given
        String nonExistentKey = "medium:test:nonexistent:" + System.currentTimeMillis();

        // when
        Mono<String> result = reactiveRedisTemplate.opsForValue().get(nonExistentKey);

        // then
        StepVerifier.create(result)
                .verifyComplete();
    }
}
