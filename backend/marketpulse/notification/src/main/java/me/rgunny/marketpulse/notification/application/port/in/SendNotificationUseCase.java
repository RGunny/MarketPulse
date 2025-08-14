package me.rgunny.marketpulse.notification.application.port.in;

import me.rgunny.marketpulse.notification.domain.event.MarketEvent;
import reactor.core.publisher.Mono;

/**
 * 알림 발송 유스케이스
 */
public interface SendNotificationUseCase {
    
    /**
     * 시장 이벤트를 받아 알림 발송
     * 
     * @param event 시장 이벤트
     * @return 발송 완료 신호
     */
    Mono<Void> sendNotification(MarketEvent event);
}