package me.rgunny.marketpulse.event.marketdata.infrastructure.adapter.out;

import lombok.RequiredArgsConstructor;
import me.rgunny.marketpulse.event.marketdata.application.port.out.SyncConfigPort;
import me.rgunny.marketpulse.event.marketdata.infrastructure.config.StockMasterSyncProperties;
import org.springframework.stereotype.Component;

/**
 * 동기화 설정 어댑터
 * 
 * Infrastructure 계층의 설정을 Application 계층으로 전달
 * 실무 패턴: 설정 변경 시 이 어댑터만 수정하면 됨
 */
@Component
@RequiredArgsConstructor
public class SyncConfigAdapter implements SyncConfigPort {
    
    private final StockMasterSyncProperties properties;
    
    @Override
    public boolean isEnabled() {
        return properties.isEnabled();
    }
    
    @Override
    public String getMode() {
        return properties.getMode();
    }
    
    @Override
    public int getBatchSize() {
        return properties.getBatchSize();
    }
    
    @Override
    public long getBatchDelayMs() {
        return properties.getBatchDelayMs();
    }
    
    @Override
    public int getApiTimeoutSeconds() {
        return properties.getApiTimeoutSeconds();
    }
    
    @Override
    public int getMaxRetryAttempts() {
        return properties.getMaxRetryAttempts();
    }
    
    @Override
    public boolean isKospiSyncEnabled() {
        return properties.isKospiSyncEnabled();
    }
    
    @Override
    public boolean isKosdaqSyncEnabled() {
        return properties.isKosdaqSyncEnabled();
    }
    
    @Override
    public double getFailureThreshold() {
        return properties.getFailureThreshold();
    }
    
    @Override
    public boolean isNotificationEnabled() {
        return properties.isNotificationEnabled();
    }
}