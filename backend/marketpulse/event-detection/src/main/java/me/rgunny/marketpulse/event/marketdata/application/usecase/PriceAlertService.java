package me.rgunny.marketpulse.event.marketdata.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.marketpulse.event.marketdata.application.port.out.AlertHistoryPort;
import me.rgunny.marketpulse.event.marketdata.domain.model.AlertHistory;
import me.rgunny.marketpulse.event.marketdata.domain.model.AlertType;
import me.rgunny.marketpulse.event.marketdata.domain.model.StockPrice;
import me.rgunny.marketpulse.event.marketdata.infrastructure.config.PriceAlertProperties;
import me.rgunny.marketpulse.event.notification.application.port.out.NotificationClientPort;
import me.rgunny.marketpulse.event.notification.application.port.out.NotificationHistoryPort;
import me.rgunny.marketpulse.event.notification.domain.model.NotificationHistory;
import me.rgunny.marketpulse.notification.grpc.NotificationServiceProto.PriceAlertType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.util.UUID;

/**
 * 가격 알림 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PriceAlertService {
    
    private final NotificationClientPort notificationClient;
    private final NotificationHistoryPort notificationHistoryPort;
    private final AlertHistoryPort alertHistoryPort;
    private final PriceAlertProperties alertProperties;
    private final Clock clock;
    
    // 기본 쿨다운: 30분
    private static final int DEFAULT_COOLDOWN_MINUTES = 30;
    private static final int LIMIT_COOLDOWN_MINUTES = 60;
    
    /**
     * 주식 가격 변동 분석 및 알림 발송 (중복 방지 적용)
     * @param stockPrice 주식 가격 정보
     * @return 알림 발송 결과
     */
    public Mono<Void> analyzeAndSendAlert(StockPrice stockPrice) {
        return Mono.defer(() -> {
            PriceAlertType alertType = determineAlertType(stockPrice);
            
            if (alertType != PriceAlertType.NONE) {
                log.info("Price alert triggered: symbol={}, alertType={}, changeRate={}%", 
                        stockPrice.getSymbol(), alertType, stockPrice.getChangeRate());
                
                // 중복 체크 후 알림 발송
                return checkCooldownAndSend(stockPrice, alertType);
            } else {
                log.debug("No alert needed for symbol={}, changeRate={}", 
                        stockPrice.getSymbol(), stockPrice.getChangeRate());
                return Mono.empty();
            }
        });
    }
    
    /**
     * 쿨다운 체크 후 알림 발송
     */
    private Mono<Void> checkCooldownAndSend(StockPrice stockPrice, PriceAlertType alertType) {
        String symbol = stockPrice.getSymbol();
        AlertType historyType = mapToAlertHistoryType(alertType);
        
        return alertHistoryPort.canSendAlert(symbol, historyType)
            .flatMap(canSend -> {
                if (!canSend) {
                    return alertHistoryPort.findBySymbolAndType(symbol, historyType)
                        .doOnNext(history -> {
                            Duration remaining = history.getRemainingCooldown(clock);
                            log.info(
                                "Alert skipped due to cooldown: symbol={}, type={}, remaining={}m {}s",
                                symbol, alertType, remaining.toMinutes(), remaining.toSecondsPart());
                        })
                        .then(Mono.empty());
                } else {
                    return sendAlertAndSaveHistory(stockPrice, alertType);
                }
            });
    }
    
    /**
     * 알림 발송 및 이력 저장
     */
    private Mono<Void> sendAlertAndSaveHistory(StockPrice stockPrice, PriceAlertType alertType) {
        String notificationId = UUID.randomUUID().toString();
        AlertType historyType = mapToAlertHistoryType(alertType);
        int cooldownMinutes = determineCooldownMinutes(alertType);
        
        return notificationClient.sendPriceAlert(stockPrice, alertType)
            .doOnSuccess(unused -> log.info("Alert sent successfully: symbol={}, type={}, price={}",
                stockPrice.getSymbol(), alertType, stockPrice.getCurrentPrice()))
            .then(alertHistoryPort.save(
                AlertHistory.create(stockPrice.getSymbol(), historyType, cooldownMinutes, clock)))
            .then(saveNotificationHistory(stockPrice, alertType, notificationId))
            .then()
            .doOnError(error -> log.error("Failed to send price alert: symbol={}, type={}, error={}", 
                stockPrice.getSymbol(), alertType, error.getMessage()));
    }
    
    /**
     * 알림 이력 저장
     */
    private Mono<NotificationHistory> saveNotificationHistory(
            StockPrice stockPrice, 
            PriceAlertType alertType, 
            String notificationId) {
        
        Duration cooldown = determineCooldownPeriod(alertType);
        
        NotificationHistory history = NotificationHistory.create(
            stockPrice.getSymbol(),
            stockPrice.getName(),
            alertType,
            stockPrice.getCurrentPrice(),
            stockPrice.getChangeRate(),
            notificationId,
            cooldown
        );
        
        return notificationHistoryPort.save(history)
            .doOnSuccess(saved -> log.debug("Notification history saved: symbol={}, type={}, cooldown={}",
                saved.symbol(), saved.alertType(), saved.cooldownPeriod()));
    }
    
    /**
     * 알림 타입별 쿨다운 기간 결정 (분 단위)
     */
    private int determineCooldownMinutes(PriceAlertType alertType) {
        return switch (alertType) {
            case LIMIT_UP, LIMIT_DOWN -> LIMIT_COOLDOWN_MINUTES;  // 상한가/하한가: 60분
            case RISE, FALL -> DEFAULT_COOLDOWN_MINUTES;          // 급등/급락: 30분
            case NONE, UNRECOGNIZED -> DEFAULT_COOLDOWN_MINUTES;
        };
    }
    
    /**
     * 알림 타입별 쿨다운 기간 결정 (Duration)
     */
    private Duration determineCooldownPeriod(PriceAlertType alertType) {
        return Duration.ofMinutes(determineCooldownMinutes(alertType));
    }
    
    /**
     * PriceAlertType을 AlertType으로 매핑
     */
    private AlertType mapToAlertHistoryType(PriceAlertType priceAlertType) {
        return switch (priceAlertType) {
            case RISE -> AlertType.PRICE_RISE;
            case FALL -> AlertType.PRICE_FALL;
            case LIMIT_UP -> AlertType.LIMIT_UP;
            case LIMIT_DOWN -> AlertType.LIMIT_DOWN;
            default -> AlertType.PRICE_RISE; // 기본값
        };
    }
    
    
    /**
     * 알림 유형 결정
     *
     * @param stockPrice 주식 가격 정보
     * @return 알림 유형 (NONE이면 알림 불필요)
     */
    private PriceAlertType determineAlertType(StockPrice stockPrice) {
        BigDecimal changeRate = stockPrice.getChangeRate();
        
        // 상한가 확인 - 설정된 임계값 이상
        if (changeRate.compareTo(alertProperties.limitUpThreshold()) >= 0) {
            return PriceAlertType.LIMIT_UP;
        }
        
        // 하한가 확인 - 설정된 임계값 이하
        if (changeRate.compareTo(alertProperties.limitDownThreshold()) <= 0) {
            return PriceAlertType.LIMIT_DOWN;
        }
        
        // 급등 확인 - 설정된 임계값 이상
        if (changeRate.compareTo(alertProperties.riseThreshold()) >= 0) {
            return PriceAlertType.RISE;
        }
        
        // 급락 확인 - 설정된 임계값 이하
        if (changeRate.compareTo(alertProperties.fallThreshold()) <= 0) {
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