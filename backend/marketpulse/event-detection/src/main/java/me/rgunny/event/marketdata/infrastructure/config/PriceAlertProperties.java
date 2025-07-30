package me.rgunny.event.marketdata.infrastructure.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

/**
 * 가격 알림 임계값 설정
 */
@ConfigurationProperties(prefix = "marketpulse.alert.price")
@Validated
public class PriceAlertProperties {
    
    @NotNull
    private BigDecimal riseThreshold;
    
    @NotNull
    private BigDecimal fallThreshold;
    
    @NotNull
    private BigDecimal limitUpThreshold;
    
    @NotNull
    private BigDecimal limitDownThreshold;
    
    // Getters and Setters
    public BigDecimal getRiseThreshold() {
        return riseThreshold;
    }
    
    public void setRiseThreshold(BigDecimal riseThreshold) {
        this.riseThreshold = riseThreshold;
    }
    
    public BigDecimal getFallThreshold() {
        return fallThreshold;
    }
    
    public void setFallThreshold(BigDecimal fallThreshold) {
        this.fallThreshold = fallThreshold;
    }
    
    public BigDecimal getLimitUpThreshold() {
        return limitUpThreshold;
    }
    
    public void setLimitUpThreshold(BigDecimal limitUpThreshold) {
        this.limitUpThreshold = limitUpThreshold;
    }
    
    public BigDecimal getLimitDownThreshold() {
        return limitDownThreshold;
    }
    
    public void setLimitDownThreshold(BigDecimal limitDownThreshold) {
        this.limitDownThreshold = limitDownThreshold;
    }
}