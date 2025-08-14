package me.rgunny.marketpulse.event.marketdata.application.port.out;

/**
 * 동기화 설정 포트
 */
public interface SyncConfigPort {
    
    /**
     * 동기화 활성화 여부
     */
    boolean isEnabled();
    
    /**
     * 동기화 모드 (FULL, INCREMENTAL)
     */
    String getMode();
    
    /**
     * 배치 처리 크기
     */
    int getBatchSize();
    
    /**
     * 배치 간 지연 시간 (밀리초)
     */
    long getBatchDelayMs();
    
    /**
     * API 호출 타임아웃 (초)
     */
    int getApiTimeoutSeconds();
    
    /**
     * 최대 재시도 횟수
     */
    int getMaxRetryAttempts();
    
    /**
     * KOSPI 개별 동기화 활성화
     */
    boolean isKospiSyncEnabled();
    
    /**
     * KOSDAQ 개별 동기화 활성화
     */
    boolean isKosdaqSyncEnabled();
    
    /**
     * 동기화 실패 임계값 (%)
     */
    double getFailureThreshold();
    
    /**
     * 동기화 결과 알림 활성화
     */
    boolean isNotificationEnabled();
}