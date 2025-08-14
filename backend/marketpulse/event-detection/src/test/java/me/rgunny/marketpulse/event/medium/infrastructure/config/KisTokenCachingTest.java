package me.rgunny.marketpulse.event.medium.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

/**
 * KIS OAuth 토큰 캐싱 Medium 테스트
 * 
 * - Spring Boot 테스트 슬라이스 사용 (@DataRedisTest)
 * - TestContainers로 실제 Redis 환경 시뮬레이션
 * - 외부 시스템과의 통합 검증
 */
@DataRedisTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("Event Detection - KIS 토큰 캐싱 테스트 (medium)")
class KisTokenCachingTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @Test
    @DisplayName("KIS OAuth 토큰 저장 시 Redis에 정상 저장되고 조회된다")
    void givenKisToken_whenCacheToken_thenTokenStoredAndRetrieved() {
        // given
        String tokenKey = "kis:oauth:token";
        String mockToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.test.token";
        Duration ttl = Duration.ofHours(24);

        // when
        Mono<String> result = reactiveRedisTemplate.opsForValue()
                .set(tokenKey, mockToken, ttl)
                .then(reactiveRedisTemplate.opsForValue().get(tokenKey))
                .doFinally(signalType ->
                    reactiveRedisTemplate.delete(tokenKey).subscribe()
                );

        // then
        StepVerifier.create(result)
                .expectNext(mockToken)
                .verifyComplete();
    }

    @Test
    @DisplayName("토큰 TTL 설정 시 자동 만료가 정상 동작한다")
    void givenTokenWithTtl_whenTtlExpires_thenTokenAutoDeleted() {
        // given
        String tokenKey = "kis:oauth:token:ttl";
        String mockToken = "short-lived-token";
        Duration shortTtl = Duration.ofSeconds(2);

        // when
        Mono<String> saveResult = reactiveRedisTemplate.opsForValue()
                .set(tokenKey, mockToken, shortTtl)
                .then(reactiveRedisTemplate.opsForValue().get(tokenKey));

        Mono<String> expiredResult = saveResult
                .delayElement(Duration.ofSeconds(3))
                .then(reactiveRedisTemplate.opsForValue().get(tokenKey));

        // then
        StepVerifier.create(saveResult)
                .expectNext(mockToken)
                .verifyComplete();

        StepVerifier.create(expiredResult)
                .verifyComplete(); // 만료되어 빈 결과
    }

    @Test
    @DisplayName("존재하지 않는 토큰 키 조회 시 빈 결과를 반환한다")
    void givenNonExistentTokenKey_whenGetToken_thenReturnsEmpty() {
        // given
        String nonExistentKey = "kis:oauth:nonexistent";

        // when
        Mono<String> result = reactiveRedisTemplate.opsForValue()
                .get(nonExistentKey);

        // then
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    @DisplayName("토큰 존재 여부 확인이 정상 동작한다")
    void givenTokenExists_whenCheckExists_thenReturnsCorrectStatus() {
        // given
        String tokenKey = "kis:oauth:token:exists";
        String mockToken = "existing-token";

        // when
        Mono<Boolean> existsResult = reactiveRedisTemplate.opsForValue()
                .set(tokenKey, mockToken, Duration.ofMinutes(5))
                .then(reactiveRedisTemplate.hasKey(tokenKey))
                .doFinally(signalType ->
                    reactiveRedisTemplate.delete(tokenKey).subscribe()
                );

        Mono<Boolean> notExistsResult = reactiveRedisTemplate
                .hasKey("kis:oauth:token:not-exists");

        // then
        StepVerifier.create(existsResult)
                .expectNext(true)
                .verifyComplete();

        StepVerifier.create(notExistsResult)
                .expectNext(false)
                .verifyComplete();
    }
}