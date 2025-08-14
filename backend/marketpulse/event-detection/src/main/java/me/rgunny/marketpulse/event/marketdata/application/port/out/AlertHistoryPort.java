package me.rgunny.marketpulse.event.marketdata.application.port.out;

import me.rgunny.marketpulse.event.marketdata.domain.model.AlertHistory;
import me.rgunny.marketpulse.event.marketdata.domain.model.AlertType;
import reactor.core.publisher.Mono;

/**
 * 알림 이력 관리 포트
 */
public interface AlertHistoryPort {
    
    /**
     * 알림 이력 저장
     * TTL은 cooldownUntil 시간까지 자동 설정
     * 
     * @param alertHistory 알림 이력
     * @return 저장된 알림 이력
     */
    Mono<AlertHistory> save(AlertHistory alertHistory);
    
    /**
     * 특정 종목의 특정 타입 알림 이력 조회
     * 
     * @param symbol 종목 코드
     * @param alertType 알림 타입
     * @return 알림 이력 (없으면 empty)
     */
    Mono<AlertHistory> findBySymbolAndType(String symbol, AlertType alertType);
    
    /**
     * 알림 가능 여부 확인
     * 쿨다운 중이 아니면 true
     * 
     * @param symbol 종목 코드
     * @param alertType 알림 타입
     * @return 알림 가능하면 true
     */
    Mono<Boolean> canSendAlert(String symbol, AlertType alertType);
    
    /**
     * 알림 이력 삭제 (쿨다운 해제)
     * 
     * @param symbol 종목 코드
     * @param alertType 알림 타입
     * @return 삭제 성공 여부
     */
    Mono<Boolean> delete(String symbol, AlertType alertType);
    
    /**
     * 특정 종목의 모든 알림 이력 삭제
     * 
     * @param symbol 종목 코드
     * @return 삭제된 개수
     */
    Mono<Long> deleteAllBySymbol(String symbol);
}