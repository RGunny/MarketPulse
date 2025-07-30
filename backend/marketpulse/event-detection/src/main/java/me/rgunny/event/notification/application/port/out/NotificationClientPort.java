package me.rgunny.event.notification.application.port.out;

import me.rgunny.event.marketdata.domain.model.StockPrice;
import me.rgunny.notification.grpc.NotificationServiceProto.PriceAlertType;
import reactor.core.publisher.Mono;

/**
 * 알림 발송 클라이언트 포트
 */
public interface NotificationClientPort {
    
    /**
     * 가격 알림 발송
     * @param stockPrice 주식 가격 정보
     * @param alertType 알림 유형
     * @return 발송 결과 (비동기)
     */
    Mono<Void> sendPriceAlert(StockPrice stockPrice, PriceAlertType alertType);
    
    /**
     * 알림 서비스 상태 확인
     * @return 서비스 상태
     */
    Mono<String> checkNotificationStatus();
}