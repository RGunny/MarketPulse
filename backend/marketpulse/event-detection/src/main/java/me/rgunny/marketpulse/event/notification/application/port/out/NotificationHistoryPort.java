package me.rgunny.marketpulse.event.notification.application.port.out;

import me.rgunny.marketpulse.event.notification.domain.model.NotificationHistory;
import me.rgunny.marketpulse.notification.grpc.NotificationServiceProto.PriceAlertType;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * 알림 이력 관리 출력 포트
 */
public interface NotificationHistoryPort {
    
    /**
     * 알림 이력 저장
     * 
     * @param history 저장할 알림 이력
     * @return 저장된 알림 이력
     */
    Mono<NotificationHistory> save(NotificationHistory history);
    
    /**
     * 최근 알림 이력 조회
     * 종목과 알림 타입으로 가장 최근 발송된 알림 조회
     * 
     * @param symbol 종목코드
     * @param alertType 알림 타입
     * @return 최근 알림 이력 (없으면 Mono.empty())
     */
    Mono<NotificationHistory> findLatest(String symbol, PriceAlertType alertType);
    
    /**
     * 쿨다운 확인
     * 해당 종목/타입의 알림이 쿨다운 중인지 확인
     * 
     * @param symbol 종목코드
     * @param alertType 알림 타입
     * @return true: 쿨다운 중 (알림 불가), false: 알림 가능
     */
    Mono<Boolean> isInCooldown(String symbol, PriceAlertType alertType);
    
    /**
     * 남은 쿨다운 시간 조회
     * 
     * @param symbol 종목코드
     * @param alertType 알림 타입
     * @return 남은 쿨다운 시간 (쿨다운 없으면 Duration.ZERO)
     */
    Mono<Duration> getRemainingCooldown(String symbol, PriceAlertType alertType);
    
    /**
     * 알림 이력 삭제
     *
     * @param symbol 종목코드
     * @param alertType 알림 타입
     * @return 삭제 완료 신호
     */
    Mono<Void> delete(String symbol, PriceAlertType alertType);
    
    /**
     * 전체 알림 이력 삭제
     *
     * @return 삭제 완료 신호
     */
    Mono<Void> deleteAll();
}