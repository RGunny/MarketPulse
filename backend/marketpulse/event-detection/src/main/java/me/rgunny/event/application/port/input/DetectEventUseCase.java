package me.rgunny.event.application.port.input;

/**
 * 이벤트 감지 Use Case
 */
public interface DetectEventUseCase {

    /**
     * 시장 이벤트 감지 실행
     */
    void detectMarketEvents();
}
