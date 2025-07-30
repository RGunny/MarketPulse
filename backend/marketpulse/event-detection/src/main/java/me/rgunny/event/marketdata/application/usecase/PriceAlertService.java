package me.rgunny.event.marketdata.application.usecase;

import me.rgunny.event.marketdata.domain.model.StockPrice;
import me.rgunny.event.marketdata.infrastructure.config.PriceAlertProperties;
import me.rgunny.event.notification.application.port.out.NotificationClientPort;
import me.rgunny.notification.grpc.NotificationServiceProto.PriceAlertType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * 가격 알림 서비스
 * - 주식 가격 변동 감지 시 알림 발송
 */
@Service
public class PriceAlertService {
    
    private static final Logger log = LoggerFactory.getLogger(PriceAlertService.class);
    
    private final NotificationClientPort notificationClient;
    private final PriceAlertProperties alertProperties;
    
    public PriceAlertService(NotificationClientPort notificationClient, PriceAlertProperties alertProperties) {
        this.notificationClient = notificationClient;
        this.alertProperties = alertProperties;
    }
    
    /**
     * 주식 가격 변동 분석 및 알림 발송
     * @param stockPrice 주식 가격 정보
     * @return 알림 발송 결과
     */
    public Mono<Void> analyzeAndSendAlert(StockPrice stockPrice) {
        return Mono.defer(() -> {
            PriceAlertType alertType = determineAlertType(stockPrice);
            
            if (alertType != PriceAlertType.NONE) {
                log.info("Price alert triggered: symbol={}, alertType={}, changeRate={}", 
                        stockPrice.getSymbol(), alertType, stockPrice.getChangeRate());
                
                return notificationClient.sendPriceAlert(stockPrice, alertType)
                        .doOnSuccess(unused -> log.info("Price alert sent successfully: symbol={}, alertType={}", 
                                stockPrice.getSymbol(), alertType))
                        .doOnError(error -> log.error("Failed to send price alert: symbol={}, alertType={}, error={}", 
                                stockPrice.getSymbol(), alertType, error.getMessage()));
            } else {
                log.debug("No alert needed for symbol={}, changeRate={}", 
                        stockPrice.getSymbol(), stockPrice.getChangeRate());
                return Mono.empty();
            }
        });
    }
    
    /**
     * 알림 유형 결정
     * @param stockPrice 주식 가격 정보
     * @return 알림 유형 (NONE이면 알림 불필요)
     */
    private PriceAlertType determineAlertType(StockPrice stockPrice) {
        BigDecimal changeRate = stockPrice.getChangeRate();
        
        // 상한가 확인 - 설정된 임계값 이상
        if (changeRate.compareTo(alertProperties.getLimitUpThreshold()) >= 0) {
            return PriceAlertType.LIMIT_UP;
        }
        
        // 하한가 확인 - 설정된 임계값 이하
        if (changeRate.compareTo(alertProperties.getLimitDownThreshold()) <= 0) {
            return PriceAlertType.LIMIT_DOWN;
        }
        
        // 급등 확인 - 설정된 임계값 이상
        if (changeRate.compareTo(alertProperties.getRiseThreshold()) >= 0) {
            return PriceAlertType.RISE;
        }
        
        // 급락 확인 - 설정된 임계값 이하
        if (changeRate.compareTo(alertProperties.getFallThreshold()) <= 0) {
            return PriceAlertType.FALL;
        }
        
        // 임계값 미달 시 알림 불필요
        return PriceAlertType.NONE;
    }
    
    /**
     * 알림 서비스 상태 확인
     * @return 서비스 상태
     */
    public Mono<String> checkNotificationServiceHealth() {
        return notificationClient.checkNotificationStatus()
                .doOnSuccess(status -> log.info("Notification service health check: {}", status))
                .doOnError(error -> log.error("Notification service health check failed: {}", error.getMessage()));
    }
}