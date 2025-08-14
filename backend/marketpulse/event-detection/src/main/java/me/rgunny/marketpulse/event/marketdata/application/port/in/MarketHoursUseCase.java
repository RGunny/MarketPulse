package me.rgunny.marketpulse.event.marketdata.application.port.in;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * 시장 거래시간 확인 유스케이스
 */
public interface MarketHoursUseCase {
    
    /**
     * 현재 시장이 열려있는지 확인
     * 
     * @return 거래 가능 시간이면 true
     */
    boolean isMarketOpen();
    
    /**
     * 특정 시간에 시장이 열려있는지 확인
     * 
     * @param dateTime 확인할 시간
     * @return 거래 가능 시간이면 true
     */
    boolean isMarketOpen(ZonedDateTime dateTime);
    
    /**
     * 다음 개장까지 남은 시간
     * 
     * @return 남은 시간
     */
    Duration getTimeUntilNextOpen();
    
    /**
     * 현재 시장 상태 조회
     * 
     * @return 시장 상태 정보
     */
    MarketStatus getMarketStatus();
    
    /**
     * 시장 상태 정보
     */
    record MarketStatus(
            boolean isOpen,
            String status,
            String description,
            Duration timeUntilNextChange
    ) {
        public static MarketStatus open(Duration untilClose) {
            return new MarketStatus(
                    true,
                    "OPEN",
                    "시장 거래 중",
                    untilClose
            );
        }
        
        public static MarketStatus closed(Duration untilOpen) {
            return new MarketStatus(
                    false,
                    "CLOSED",
                    "시장 마감",
                    untilOpen
            );
        }
        
        public static MarketStatus weekend(Duration untilOpen) {
            return new MarketStatus(
                    false,
                    "WEEKEND",
                    "주말 휴장",
                    untilOpen
            );
        }
        
        public static MarketStatus holiday(Duration untilOpen) {
            return new MarketStatus(
                    false,
                    "HOLIDAY",
                    "공휴일 휴장",
                    untilOpen
            );
        }
    }
}