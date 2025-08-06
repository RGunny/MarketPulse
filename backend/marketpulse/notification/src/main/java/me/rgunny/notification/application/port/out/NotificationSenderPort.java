package me.rgunny.notification.application.port.out;

import me.rgunny.notification.domain.model.Notification;
import me.rgunny.notification.domain.model.NotificationChannel;
import reactor.core.publisher.Mono;

/**
 * 알림 발송 포트
 */
public interface NotificationSenderPort {
    
    /**
     * 알림 발송
     * 
     * @param notification 알림 정보
     * @return 발송 완료 신호
     */
    Mono<Void> send(Notification notification);
    
    /**
     * 지원하는 채널 확인
     * 
     * @param channel 알림 채널
     * @return 지원 여부
     */
    boolean supports(NotificationChannel channel);
}