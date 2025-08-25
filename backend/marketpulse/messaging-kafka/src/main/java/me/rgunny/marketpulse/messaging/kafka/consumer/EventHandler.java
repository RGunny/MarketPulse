package me.rgunny.marketpulse.messaging.kafka.consumer;

/**
 * 이벤트 핸들러 인터페이스
 */
public interface EventHandler<T> {
    
    /**
     * 이벤트 처리 가능 여부 확인
     */
    boolean canHandle(Object event);
    
    /**
     * 이벤트 처리
     */
    void handle(T event);
    
    /**
     * 처리 우선순위 (낮을수록 먼저 처리)
     */
    default int getOrder() {
        return 0;
    }
}