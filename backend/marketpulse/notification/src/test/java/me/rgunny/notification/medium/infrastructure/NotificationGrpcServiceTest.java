package me.rgunny.notification.medium.infrastructure;

import io.grpc.stub.StreamObserver;
import me.rgunny.notification.application.port.in.SendNotificationUseCase;
import me.rgunny.notification.grpc.NotificationServiceProto.*;
import me.rgunny.notification.infrastructure.adapter.in.grpc.NotificationGrpcService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

/**
 * NotificationGrpcService 단위 테스트
 * - gRPC 어댑터 테스트
 * - 헥사고날 아키텍처 인바운드 어댑터 검증
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationGrpcService gRPC 어댑터")
class NotificationGrpcServiceTest {
    
    @Mock
    private SendNotificationUseCase notificationUseCase;
    
    @Mock
    private StreamObserver<NotificationResponse> responseObserver;
    
    @Mock 
    private StreamObserver<NotificationStatusResponse> statusResponseObserver;
    
    private NotificationGrpcService grpcService;
    
    @BeforeEach
    void setUp() {
        grpcService = new NotificationGrpcService(notificationUseCase, "1.0.0");
    }
    
    @Test
    @DisplayName("가격 알림 요청 수신 시 도메인 서비스를 호출하고 성공 응답을 반환한다")
    void givenPriceAlertRequest_whenSendPriceAlert_thenCallsUseCaseAndReturnsSuccess() {
        // given
        PriceAlertRequest request = PriceAlertRequest.newBuilder()
                .setEventId("event-123")
                .setSymbol("005930")
                .setSymbolName("삼성전자")
                .setCurrentPrice(72000.0)
                .setPreviousPrice(71500.0)
                .setChangeRate(0.7)
                .setAlertType(PriceAlertType.RISE)
                .setTimestamp(Instant.now().toEpochMilli())
                .putMetadata("threshold", "5%")
                .build();
        
        given(notificationUseCase.sendNotification(any())).willReturn(Mono.empty());
        
        // when
        grpcService.sendPriceAlert(request, responseObserver);
        
        // then
        verify(notificationUseCase).sendNotification(any());
        
        ArgumentCaptor<NotificationResponse> responseCaptor = 
                ArgumentCaptor.forClass(NotificationResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();
        
        NotificationResponse response = responseCaptor.getValue();
        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Price alert sent successfully");
        assertThat(response.getNotificationId()).isNotEmpty();
        assertThat(response.getProcessedAt()).isGreaterThan(0);
    }
    
    @Test
    @DisplayName("도메인 서비스에서 오류 발생 시 실패 응답을 반환한다")
    void givenUseCaseError_whenSendPriceAlert_thenReturnsFailureResponse() {
        // given
        PriceAlertRequest request = PriceAlertRequest.newBuilder()
                .setEventId("event-123")
                .setSymbol("005930")
                .setSymbolName("삼성전자")
                .setCurrentPrice(72000.0)
                .setPreviousPrice(71500.0)
                .setChangeRate(0.7)
                .setAlertType(PriceAlertType.RISE)
                .setTimestamp(Instant.now().toEpochMilli())
                .build();
        
        RuntimeException error = new RuntimeException("Slack API Error");
        given(notificationUseCase.sendNotification(any())).willReturn(Mono.error(error));
        
        // when
        grpcService.sendPriceAlert(request, responseObserver);
        
        // then
        ArgumentCaptor<NotificationResponse> responseCaptor = 
                ArgumentCaptor.forClass(NotificationResponse.class);
        verify(responseObserver).onNext(responseCaptor.capture());
        verify(responseObserver).onCompleted();
        
        NotificationResponse response = responseCaptor.getValue();
        assertThat(response.getSuccess()).isFalse();
        assertThat(response.getMessage()).contains("Failed to send alert");
        assertThat(response.getNotificationId()).isEmpty();
    }
    
    @Test
    @DisplayName("헬스 체크 요청 시 서비스 상태를 반환한다")
    void givenHealthCheckRequest_whenGetNotificationStatus_thenReturnsHealthStatus() {
        // given
        Empty request = Empty.newBuilder().build();
        
        // when
        grpcService.getNotificationStatus(request, statusResponseObserver);
        
        // then
        ArgumentCaptor<NotificationStatusResponse> responseCaptor = 
                ArgumentCaptor.forClass(NotificationStatusResponse.class);
        verify(statusResponseObserver).onNext(responseCaptor.capture());
        verify(statusResponseObserver).onCompleted();
        
        NotificationStatusResponse response = responseCaptor.getValue();
        assertThat(response.getStatus()).isEqualTo(NotificationStatus.HEALTHY);
        assertThat(response.getVersion()).isEqualTo("1.0.0");
        assertThat(response.getUptimeSeconds()).isGreaterThanOrEqualTo(0);
    }
}