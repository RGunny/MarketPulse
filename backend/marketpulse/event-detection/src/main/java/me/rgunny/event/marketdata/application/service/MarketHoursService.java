package me.rgunny.event.marketdata.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.event.marketdata.application.port.in.MarketHoursUseCase;
import me.rgunny.event.marketdata.domain.model.MarketHours;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * 시장 거래시간 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketHoursService implements MarketHoursUseCase {
    
    private final MarketHours marketHours;
    private final Clock clock;
    
    @Override
    public boolean isMarketOpen() {
        ZonedDateTime now = ZonedDateTime.now(clock);
        boolean isOpen = marketHours.isMarketOpen(now);
        
        if (log.isDebugEnabled()) {
            log.debug("Market open check at {}: {}", now, isOpen);
        }
        return isOpen;
    }
    
    @Override
    public boolean isMarketOpen(ZonedDateTime dateTime) {
        return marketHours.isMarketOpen(dateTime);
    }
    
    @Override
    public Duration getTimeUntilNextOpen() {
        ZonedDateTime now = ZonedDateTime.now(clock);
        return marketHours.untilNextOpen(now);
    }
    
    @Override
    public MarketStatus getMarketStatus() {
        ZonedDateTime now = ZonedDateTime.now(clock);
        
        if (marketHours.isMarketOpen(now)) {
            Duration untilClose = marketHours.untilClose(now);
            return MarketStatus.open(untilClose);
        }
        
        Duration untilOpen = marketHours.untilNextOpen(now);

        if (marketHours.isWeekend(now)) {
            return MarketStatus.weekend(untilOpen);
        }
        
        if (marketHours.isHoliday(now)) {
            return MarketStatus.holiday(untilOpen);
        }
        
        return MarketStatus.closed(untilOpen);
    }
}