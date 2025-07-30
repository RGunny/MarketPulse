package me.rgunny.event.notification.infrastructure.adapter.out.grpc;

import io.grpc.StatusRuntimeException;
import me.rgunny.event.marketdata.domain.model.StockPrice;
import me.rgunny.event.notification.application.port.out.NotificationClientPort;
import me.rgunny.notification.grpc.NotificationServiceGrpc;
import me.rgunny.notification.grpc.NotificationServiceProto.PriceAlertRequest;
import me.rgunny.notification.grpc.NotificationServiceProto.NotificationResponse;
import me.rgunny.notification.grpc.NotificationServiceProto.Empty;
import me.rgunny.notification.grpc.NotificationServiceProto.NotificationStatusResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Notification gRPC 클라이언트 어댑터
 */
@Component
public class NotificationGrpcClientAdapter implements NotificationClientPort {
    
    private static final Logger log = LoggerFactory.getLogger(NotificationGrpcClientAdapter.class);
    
    @GrpcClient("notification")
    private NotificationServiceGrpc.NotificationServiceBlockingStub notificationStub;
    
    @Override
    public Mono<Void> sendPriceAlert(StockPrice stockPrice, me.rgunny.notification.grpc.NotificationServiceProto.PriceAlertType alertType) {
        return Mono.fromCallable(() -> {
            try {
                PriceAlertRequest request = buildPriceAlertRequest(stockPrice, alertType);
                
                log.info("Sending price alert via gRPC: symbol={}, alertType={}", 
                        stockPrice.getSymbol(), alertType);
                
                NotificationResponse response = notificationStub.sendPriceAlert(request);
                
                if (response.getSuccess()) {
                    log.info("Price alert sent successfully: notificationId={}, message={}", 
                            response.getNotificationId(), response.getMessage());
                } else {
                    log.warn("Price alert failed: message={}", response.getMessage());
                    throw new RuntimeException("Failed to send price alert: " + response.getMessage());
                }
                
                return null;
            } catch (StatusRuntimeException e) {
                log.error("gRPC call failed: code={}, description={}", 
                        e.getStatus().getCode(), e.getStatus().getDescription());
                throw new RuntimeException("gRPC communication failed: " + e.getMessage(), e);
            }
        });
    }
    
    @Override
    public Mono<String> checkNotificationStatus() {
        return Mono.fromCallable(() -> {
            try {
                NotificationStatusResponse response = notificationStub.getNotificationStatus(Empty.newBuilder().build());
                log.info("Notification service status: {}, version: {}", 
                        response.getStatus(), response.getVersion());
                return response.getStatus().name();
            } catch (StatusRuntimeException e) {
                log.error("Failed to check notification status: {}", e.getMessage());
                return "UNKNOWN";
            }
        });
    }
    
    private PriceAlertRequest buildPriceAlertRequest(StockPrice stockPrice, me.rgunny.notification.grpc.NotificationServiceProto.PriceAlertType alertType) {
        return PriceAlertRequest.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setSymbol(stockPrice.getSymbol())
                .setSymbolName(stockPrice.getName())
                .setCurrentPrice(stockPrice.getCurrentPrice().doubleValue())
                .setPreviousPrice(stockPrice.getPreviousClose().doubleValue())
                .setChangeRate(calculateChangeRate(stockPrice).doubleValue())
                .setAlertType(alertType)
                .setTimestamp(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) * 1000)
                .putMetadata("high", stockPrice.getHigh().toString())
                .putMetadata("low", stockPrice.getLow().toString())
                .putMetadata("volume", String.valueOf(stockPrice.getVolume()))
                .build();
    }
    
    private BigDecimal calculateChangeRate(StockPrice stockPrice) {
        if (stockPrice.getPreviousClose().compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return stockPrice.getChange()
                .divide(stockPrice.getPreviousClose(), 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));
    }
}