package me.rgunny.notification.infrastructure.adapter.in.grpc;

import io.grpc.stub.StreamObserver;
import me.rgunny.notification.application.port.in.SendNotificationUseCase;
import me.rgunny.notification.domain.event.PriceAlertEvent;
import me.rgunny.notification.grpc.NotificationServiceGrpc;
import me.rgunny.notification.grpc.NotificationServiceProto.Empty;
import me.rgunny.notification.grpc.NotificationServiceProto.NotificationResponse;
import me.rgunny.notification.grpc.NotificationServiceProto.NotificationStatus;
import me.rgunny.notification.grpc.NotificationServiceProto.NotificationStatusResponse;
import me.rgunny.notification.grpc.NotificationServiceProto.PriceAlertRequest;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;

/**
 * gRPC 알림 서비스 구현체
 * - event-detection에서 gRPC 호출을 수신하여 알림 발송
 */
@GrpcService
public class NotificationGrpcService extends NotificationServiceGrpc.NotificationServiceImplBase {
    
    private static final Logger log = LoggerFactory.getLogger(NotificationGrpcService.class);
    
    private final SendNotificationUseCase notificationUseCase;
    private final String serviceVersion;
    private final Instant startTime;
    
    public NotificationGrpcService(
            SendNotificationUseCase notificationUseCase,
            @Value("${spring.application.version:1.0.0}") String serviceVersion) {
        this.notificationUseCase = notificationUseCase;
        this.serviceVersion = serviceVersion;
        this.startTime = Instant.now();
    }
    
    @Override
    public void sendPriceAlert(PriceAlertRequest request, StreamObserver<NotificationResponse> responseObserver) {
        log.info("Received gRPC price alert request: eventId={}, symbol={}", 
                request.getEventId(), request.getSymbol());
        
        try {
            // gRPC 요청을 도메인 이벤트로 변환
            PriceAlertEvent event = convertToEvent(request);
            
            // 비동기 알림 발송
            notificationUseCase.sendNotification(event)
                    .doOnSuccess(unused -> {
                        NotificationResponse response = NotificationResponse.newBuilder()
                                .setSuccess(true)
                                .setMessage("Price alert sent successfully")
                                .setNotificationId(UUID.randomUUID().toString())
                                .setProcessedAt(Instant.now().toEpochMilli())
                                .build();
                        
                        responseObserver.onNext(response);
                        responseObserver.onCompleted();
                        
                        log.info("Successfully sent price alert: eventId={}", request.getEventId());
                    })
                    .doOnError(error -> {
                        log.error("Failed to send price alert: eventId={}", request.getEventId(), error);
                        
                        NotificationResponse response = NotificationResponse.newBuilder()
                                .setSuccess(false)
                                .setMessage("Failed to send alert: " + error.getMessage())
                                .setNotificationId("")
                                .setProcessedAt(Instant.now().toEpochMilli())
                                .build();
                        
                        responseObserver.onNext(response);
                        responseObserver.onCompleted();
                    })
                    .subscribe();
                    
        } catch (Exception e) {
            log.error("Error processing gRPC request: eventId={}", request.getEventId(), e);
            
            NotificationResponse response = NotificationResponse.newBuilder()
                    .setSuccess(false)
                    .setMessage("Internal error: " + e.getMessage())
                    .setNotificationId("")
                    .setProcessedAt(Instant.now().toEpochMilli())
                    .build();
            
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
    
    @Override
    public void getNotificationStatus(Empty request, StreamObserver<NotificationStatusResponse> responseObserver) {
        log.debug("Health check requested via gRPC");
        
        long uptimeSeconds = Instant.now().getEpochSecond() - startTime.getEpochSecond();
        
        NotificationStatusResponse response = NotificationStatusResponse.newBuilder()
                .setStatus(NotificationStatus.HEALTHY)
                .setVersion(serviceVersion)
                .setActiveNotifications(0)
                .setUptimeSeconds(uptimeSeconds)
                .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
    
    /**
     * gRPC 요청을 도메인 이벤트로 변환
     */
    private PriceAlertEvent convertToEvent(PriceAlertRequest request) {
        LocalDateTime timestamp = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(request.getTimestamp()),
                ZoneId.systemDefault()
        );
        
        return new PriceAlertEvent(
                request.getEventId(),
                request.getSymbol(),
                request.getSymbolName(),
                BigDecimal.valueOf(request.getPreviousPrice()),
                BigDecimal.valueOf(request.getCurrentPrice()),
                BigDecimal.valueOf(request.getChangeRate()),
                request.getAlertType().name(),
                timestamp,
                Map.copyOf(request.getMetadataMap())
        );
    }
}