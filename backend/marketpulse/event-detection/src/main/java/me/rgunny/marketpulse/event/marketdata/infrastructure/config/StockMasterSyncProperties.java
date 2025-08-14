package me.rgunny.marketpulse.event.marketdata.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 종목 마스터 동기화 설정
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "marketpulse.stock-master.sync")
public class StockMasterSyncProperties {
    
    /**
     * 동기화 활성화 여부
     */
    private boolean enabled = false;
    
    /**
     * 동기화 모드 (FULL, INCREMENTAL)
     */
    private String mode = "INCREMENTAL";
    
    /**
     * 전체 동기화 크론 표현식
     * 기본값: 매일 새벽 2시
     */
    private String fullSyncCron = "0 0 2 * * *";
    
    /**
     * 증분 동기화 크론 표현식
     * 기본값: 매시간 정각
     */
    private String incrementalSyncCron = "0 0 * * * *";
    
    /**
     * KOSPI 동기화 크론 표현식
     * 기본값: 매일 새벽 3시
     */
    private String kospiSyncCron = "0 0 3 * * *";
    
    /**
     * KOSDAQ 동기화 크론 표현식
     * 기본값: 매일 새벽 3시 30분
     */
    private String kosdaqSyncCron = "0 30 3 * * *";
    
    /**
     * 배치 처리 크기
     */
    private int batchSize = 100;
    
    /**
     * 배치 간 지연 시간 (밀리초)
     */
    private long batchDelayMs = 100;
    
    /**
     * API 호출 타임아웃 (초)
     */
    private int apiTimeoutSeconds = 30;
    
    /**
     * 최대 재시도 횟수
     */
    private int maxRetryAttempts = 3;
    
    /**
     * KOSPI 개별 동기화 활성화
     */
    private boolean kospiSyncEnabled = false;
    
    /**
     * KOSDAQ 개별 동기화 활성화
     */
    private boolean kosdaqSyncEnabled = false;
    
    /**
     * 동기화 실패 임계값 (%)
     * 이 값을 초과하면 알림 발송
     */
    private double failureThreshold = 10.0;
    
    /**
     * 동기화 결과 알림 활성화
     */
    private boolean notificationEnabled = true;
}