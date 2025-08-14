package me.rgunny.marketpulse.notification.unit.application;

import me.rgunny.marketpulse.notification.application.port.out.NotificationMetricsPort;
import me.rgunny.marketpulse.notification.application.port.out.NotificationSenderPort;
import me.rgunny.marketpulse.notification.application.usecase.NotificationService;
import me.rgunny.marketpulse.notification.domain.event.NotificationAuditEvent;
import me.rgunny.marketpulse.notification.domain.event.PriceAlertEvent;
import me.rgunny.marketpulse.notification.domain.model.Notification;
import me.rgunny.marketpulse.notification.domain.model.NotificationChannel;
import me.rgunny.marketpulse.notification.fixture.NotificationTestFixture;
import me.rgunny.marketpulse.common.core.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

/**
 * NotificationService 애플리케이션 서비스 단위 테스트
 * - 이벤트 기반 이력 관리
 * - 메트릭 수집 검증
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService 애플리케이션 서비스")
class NotificationServiceTest {
    
    @Mock
    private NotificationSenderPort slackSender;
    
    @Mock
    private ApplicationEventPublisher eventPublisher;
    
    @Mock
    private NotificationMetricsPort metrics;
    
    private NotificationService notificationService;
    
    @BeforeEach
    void setUp() {
        given(slackSender.supports(NotificationChannel.SLACK)).willReturn(true);
        given(metrics.startTimer()).willReturn(null); // Timer.Sample mock
        
        notificationService = new NotificationService(
                List.of(slackSender),
                eventPublisher,
                metrics,
                "test-channel"
        );
    }
    
    @Test
    @DisplayName("가격 알림 이벤트 수신 시 Slack으로 알림을 발송하고 성공 이벤트를 발행한다")
    void givenPriceAlertEvent_whenSendNotification_thenSendsSlackAndPublishesSuccessEvent() {
        // given
        PriceAlertEvent event = NotificationTestFixture.createPriceAlertEvent();
        given(slackSender.send(any(Notification.class))).willReturn(Mono.empty());
        
        // when
        Mono<Void> result = notificationService.sendNotification(event);
        
        // then
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(slackSender).send(any(Notification.class));
        verify(metrics).recordSuccess(any(), any());
        verify(eventPublisher).publishEvent(any(NotificationAuditEvent.class));
        
        // 이벤트 내용 검증
        ArgumentCaptor<NotificationAuditEvent> eventCaptor = ArgumentCaptor.forClass(NotificationAuditEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        NotificationAuditEvent publishedEvent = eventCaptor.getValue();
        
        assertThat(publishedEvent.status()).isEqualTo("SUCCESS");
        assertThat(publishedEvent.eventId()).isEqualTo(event.eventId());
    }
    
    @Test
    @DisplayName("Slack 발송 실패 시 실패 메트릭과 이벤트를 발행한다")
    void givenSlackSendError_whenSendNotification_thenRecordsFailureAndPublishesFailedEvent() {
        // given
        PriceAlertEvent event = NotificationTestFixture.createPriceAlertEvent();
        RuntimeException slackError = new RuntimeException("Slack API Error");
        given(slackSender.send(any(Notification.class))).willReturn(Mono.error(slackError));
        
        // when
        Mono<Void> result = notificationService.sendNotification(event);
        
        // then
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
        
        verify(slackSender).send(any(Notification.class));
        verify(metrics).recordFailure(any(), any(), eq("RuntimeException"));
        verify(eventPublisher).publishEvent(any(NotificationAuditEvent.class));
        
        // 실패 이벤트 검증
        ArgumentCaptor<NotificationAuditEvent> eventCaptor = ArgumentCaptor.forClass(NotificationAuditEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        NotificationAuditEvent publishedEvent = eventCaptor.getValue();
        
        assertThat(publishedEvent.status()).isEqualTo("FAILED");
        assertThat(publishedEvent.errorMessage()).isEqualTo("Slack API Error");
    }
    
    @Test
    @DisplayName("지원하지 않는 채널인 경우 BusinessException이 발생한다")
    void givenUnsupportedChannel_whenSendNotification_thenThrowsBusinessException() {
        // given
        PriceAlertEvent event = NotificationTestFixture.createPriceAlertEvent();
        given(slackSender.supports(NotificationChannel.SLACK)).willReturn(false);
        
        // when
        Mono<Void> result = notificationService.sendNotification(event);
        
        // then
        StepVerifier.create(result)
                .expectError(BusinessException.class)
                .verify();
        
        verify(slackSender, never()).send(any(Notification.class));
        verify(metrics).recordFailure(any(), any(), eq("BusinessException"));
        verify(eventPublisher).publishEvent(any(NotificationAuditEvent.class));
    }
    
}