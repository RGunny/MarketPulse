package me.rgunny.event.unit.infrastructure.adapter.output;

import me.rgunny.event.infrastructure.adapter.output.RedisKISTokenCacheAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisKISTokenCacheAdapter - 토큰 캐시 어댑터 (unit)")
class RedisKISTokenCacheAdapterTest {

    @Mock
    private ReactiveRedisTemplate<String, String> redisTemplate;

    @Mock
    private ReactiveValueOperations<String, String> valueOperations;

    private RedisKISTokenCacheAdapter tokenCacheAdapter;

    private static final String TOKEN_KEY = "kis:oauth:token";

    @BeforeEach
    void setUp() {
        tokenCacheAdapter = new RedisKISTokenCacheAdapter(redisTemplate);
    }

    @Test
    @DisplayName("토큰 저장 후 조회 시 저장된 토큰을 반환한다")
    void givenSavedToken_whenGetToken_thenReturnsToken() {
        // given
        String expectedToken = "test-token";
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.get(TOKEN_KEY)).willReturn(Mono.just(expectedToken));

        // when
        Mono<String> result = tokenCacheAdapter.getToken();

        // then
        StepVerifier.create(result)
                .expectNext(expectedToken)
                .verifyComplete();

        then(redisTemplate).should().opsForValue();
        then(valueOperations).should().get(TOKEN_KEY);
    }

    @Test
    @DisplayName("토큰 저장 시 TTL과 함께 저장된다")
    void givenTokenWithTtl_whenSaveToken_thenTokenIsSavedWithExpiration() {
        // given
        String token = "test-token";
        Duration ttl = Duration.ofHours(1);
        given(redisTemplate.opsForValue()).willReturn(valueOperations);
        given(valueOperations.set(TOKEN_KEY, token, ttl)).willReturn(Mono.just(true));

        // when
        Mono<Void> result = tokenCacheAdapter.saveToken(token, ttl);

        // then
        StepVerifier.create(result)
                .verifyComplete();

        then(redisTemplate).should().opsForValue();
        then(valueOperations).should().set(TOKEN_KEY, token, ttl);
    }

    @Test
    @DisplayName("토큰 존재 시 유효성 검사에서 true를 반환한다")
    void givenExistingToken_whenIsTokenValid_thenReturnsTrue() {
        // given
        given(redisTemplate.hasKey(TOKEN_KEY)).willReturn(Mono.just(true));

        // when
        Mono<Boolean> result = tokenCacheAdapter.isTokenValid();

        // then
        StepVerifier.create(result)
                .expectNext(true)
                .verifyComplete();

        then(redisTemplate).should().hasKey(TOKEN_KEY);
    }

    @Test
    @DisplayName("토큰 미존재 시 유효성 검사에서 false를 반환한다")
    void givenNoToken_whenIsTokenValid_thenReturnsFalse() {
        // given
        given(redisTemplate.hasKey(TOKEN_KEY)).willReturn(Mono.just(false));

        // when
        Mono<Boolean> result = tokenCacheAdapter.isTokenValid();

        // then
        StepVerifier.create(result)
                .expectNext(false)
                .verifyComplete();

        then(redisTemplate).should().hasKey(TOKEN_KEY);
    }

    @Test
    @DisplayName("토큰 TTL 조회 시 남은 만료 시간을 반환한다")
    void givenTokenWithTtl_whenGetTokenTtl_thenReturnsRemainingTimeInSeconds() {
        // given
        Duration ttlDuration = Duration.ofMinutes(30);
        Long expectedSeconds = ttlDuration.getSeconds();
        given(redisTemplate.getExpire(TOKEN_KEY)).willReturn(Mono.just(ttlDuration));

        // when
        Mono<Long> result = tokenCacheAdapter.getTokenTtl();

        // then
        StepVerifier.create(result)
                .expectNext(expectedSeconds)
                .verifyComplete();

        then(redisTemplate).should().getExpire(TOKEN_KEY);
    }

    @Test
    @DisplayName("토큰 삭제 시 Redis에서 키가 삭제된다")
    void givenExistingToken_whenClearToken_thenTokenIsDeleted() {
        // given
        given(redisTemplate.delete(TOKEN_KEY)).willReturn(Mono.just(1L));

        // when
        Mono<Void> result = tokenCacheAdapter.clearToken();

        // then
        StepVerifier.create(result)
                .verifyComplete();

        then(redisTemplate).should().delete(TOKEN_KEY);
    }
}
