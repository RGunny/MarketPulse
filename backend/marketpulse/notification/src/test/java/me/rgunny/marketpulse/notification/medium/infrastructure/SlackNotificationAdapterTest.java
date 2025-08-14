package me.rgunny.marketpulse.notification.medium.infrastructure;

import me.rgunny.marketpulse.notification.domain.model.NotificationChannel;
import me.rgunny.marketpulse.notification.infrastructure.adapter.out.slack.SlackNotificationAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SlackNotificationAdapter 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SlackNotificationAdapter 인프라스트럭처 어댑터")
class SlackNotificationAdapterTest {
    
    private SlackNotificationAdapter adapter;
    
    @BeforeEach
    void setUp() {
        adapter = new SlackNotificationAdapter("https://hooks.slack.com/test-webhook");
    }
    
    @Test
    @DisplayName("SLACK 채널을 지원한다")
    void givenSlackChannel_whenSupports_thenReturnsTrue() {
        // given
        NotificationChannel channel = NotificationChannel.SLACK;
        
        // when
        boolean supports = adapter.supports(channel);
        
        // then
        assertThat(supports).isTrue();
    }
    
    @Test
    @DisplayName("SLACK이 아닌 다른 채널은 지원하지 않는다")
    void givenNonSlackChannel_whenSupports_thenReturnsFalse() {
        // given
        NotificationChannel channel = NotificationChannel.EMAIL;
        
        // when
        boolean supports = adapter.supports(channel);
        
        // then
        assertThat(supports).isFalse();
    }
    
}