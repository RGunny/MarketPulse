package me.rgunny.marketpulse.unit.config;

import me.rgunny.marketpulse.common.config.RedisConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RedisConfig - 설정 빈 생성 테스트 (unit)")
class RedisConfigTest {

    private RedisConfig redisConfig;

    @BeforeEach
    void setUp() {
        redisConfig = new RedisConfig();
        ReflectionTestUtils.setField(redisConfig, "redisHost", "localhost");
        ReflectionTestUtils.setField(redisConfig, "redisPort", 6379);
        ReflectionTestUtils.setField(redisConfig, "redisPassword", "");
    }

    @Test
    @DisplayName("기본 Redis 설정으로 LettuceConnectionFactory 빈이 생성된다")
    void givenDefaultRedisProperties_whenCreateConnectionFactory_thenLettuceFactoryCreated() {
        // given - setUp에서 설정됨

        // when
        ReactiveRedisConnectionFactory connectionFactory = redisConfig.reactiveRedisConnectionFactory();

        // then
        assertThat(connectionFactory).isNotNull();
        assertThat(connectionFactory).isInstanceOf(LettuceConnectionFactory.class);

        LettuceConnectionFactory lettuceFactory = (LettuceConnectionFactory) connectionFactory;
        assertThat(lettuceFactory.getHostName()).isEqualTo("localhost");
        assertThat(lettuceFactory.getPort()).isEqualTo(6379);
    }

    @Test
    @DisplayName("Redis 패스워드가 설정된 경우 ConnectionFactory에 패스워드가 적용된다")
    void givenRedisPassword_whenCreateConnectionFactory_thenPasswordConfigured() {
        // given
        ReflectionTestUtils.setField(redisConfig, "redisPassword", "test-password");

        // when
        ReactiveRedisConnectionFactory connectionFactory = redisConfig.reactiveRedisConnectionFactory();

        // then
        assertThat(connectionFactory).isInstanceOf(LettuceConnectionFactory.class);
        LettuceConnectionFactory lettuceFactory = (LettuceConnectionFactory) connectionFactory;
        assertThat(lettuceFactory.getPassword()).isEqualTo("test-password");
    }

    @Test
    @DisplayName("빈 패스워드일 때 ConnectionFactory에 패스워드가 설정되지 않는다")
    void givenEmptyPassword_whenCreateConnectionFactory_thenNoPasswordSet() {
        // given
        ReflectionTestUtils.setField(redisConfig, "redisPassword", "   ");

        // when
        ReactiveRedisConnectionFactory connectionFactory = redisConfig.reactiveRedisConnectionFactory();

        // then
        assertThat(connectionFactory).isInstanceOf(LettuceConnectionFactory.class);
        LettuceConnectionFactory lettuceFactory = (LettuceConnectionFactory) connectionFactory;
        assertThat(lettuceFactory.getPassword()).isNull();
    }
}
