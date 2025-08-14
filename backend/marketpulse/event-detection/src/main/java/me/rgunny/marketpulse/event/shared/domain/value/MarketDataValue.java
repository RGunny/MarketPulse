package me.rgunny.marketpulse.event.shared.domain.value;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 시장 데이터의 공통 인터페이스
 * 주식, 환율, 원자재 등 모든 시장 데이터가 구현해야 하는 기본 계약
 */
public interface MarketDataValue {
    
    /**
     * 시장 데이터의 고유 식별자 (종목코드, 통화코드 등)
     */
    String getSymbol();
    
    /**
     * 시장 데이터의 이름
     */
    String getName();
    
    /**
     * 현재 값 (가격, 환율, 지수 등)
     */
    BigDecimal getCurrentValue();
    
    /**
     * 전일/이전 값 대비 변화량
     */
    BigDecimal getChange();
    
    /**
     * 전일/이전 값 대비 변화율 (%)
     */
    BigDecimal getChangeRate();
    
    /**
     * 시장 데이터 타입
     */
    MarketDataType getMarketDataType();
    
    /**
     * 데이터 수집 시간
     */
    LocalDateTime getTimestamp();
    
    /**
     * 유의미한 변화인지 판단
     * @param threshold 임계값 (%)
     * @return 임계값 이상의 변화 여부
     */
    default boolean isSignificantChange(BigDecimal threshold) {
        if (getChangeRate() == null || threshold == null) {
            return false;
        }
        return getChangeRate().abs().compareTo(threshold) >= 0;
    }
    
    /**
     * 상승 여부
     */
    default boolean isUp() {
        return getChange() != null && getChange().compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * 하락 여부
     */
    default boolean isDown() {
        return getChange() != null && getChange().compareTo(BigDecimal.ZERO) < 0;
    }
}