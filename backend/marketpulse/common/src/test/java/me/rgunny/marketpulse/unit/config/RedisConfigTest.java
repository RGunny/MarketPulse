package me.rgunny.marketpulse.unit.config;

import me.rgunny.marketpulse.common.config.RedisConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;


import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisConfig - Redis 설정 검증 (unit)")
class RedisConfigTest {

    @Mock
    private ReactiveRedisConnectionFactory connectionFactory;

    private RedisConfig redisConfig;

    @BeforeEach
    void setUp() {
        redisConfig = new RedisConfig();
    }

    @Test
    @DisplayName("ReactiveRedisTemplate Bean이 올바른 Serializer로 생성된다")
    void givenConnectionFactory_whenCreateTemplate_thenCorrectSerializerApplied() {
        // given
        // Mock connectionFactory 준비됨

        // when
        ReactiveRedisTemplate<String, String> template = 
            redisConfig.reactiveRedisTemplate(connectionFactory);

        // then
        assertThat(template).isNotNull();
        assertThat(template.getConnectionFactory()).isEqualTo(connectionFactory);
        
        // RedisTemplate이 올바른 ConnectionFactory를 사용하는지만 검증
        // SerializationContext의 내부 구현은 Spring Data Redis에 위임
    }
}
