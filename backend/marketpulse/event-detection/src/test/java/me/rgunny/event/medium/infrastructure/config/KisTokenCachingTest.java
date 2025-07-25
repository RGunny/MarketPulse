package me.rgunny.event.medium.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
@DisplayName("Event Detection - KIS 토큰 캐싱 테스트 (medium)")
class KisTokenCachingTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public WebClient webClient() {
            // Medium 테스트용 WebClient Bean 등록 (외부 API 호출 없이 테스트)
            return WebClient.builder()
                    .baseUrl("http://localhost:8080")
                    .build();
        }
    }

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    @Qualifier("reactiveRedisTemplate")
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
