package me.rgunny.marketpulse.event.marketdata.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import me.rgunny.marketpulse.event.marketdata.domain.model.MarketHours;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * 시간 관련 설정
 */
@Slf4j
@Configuration
public class TimeConfig {

    /**
     * 기본 시스템 Clock
     * 실제 서비스 및 로컬 개발에서 사용
     */
    @Bean
    @ConditionalOnMissingBean(Clock.class)
    public Clock systemClock(@Value("${app.timezone:Asia/Seoul}") String timezone) {
        ZoneId zoneId = ZoneId.of(timezone);
        log.info("System clock initialized with timezone: {}", zoneId);
        return Clock.system(zoneId);
    }


    /**
     * MarketHours Bean 등록
     * 시장별 거래시간 설정 관리
     * 
     * @param market 시장 구분 (기본값: KOREA_STOCK_MARKET)
     */
    @Bean
    @ConditionalOnMissingBean(MarketHours.class)
    public MarketHours marketHours(@Value("${app.market:KOREA_STOCK_MARKET}") String market) {
        MarketHours selected = switch (market) {
            case "KOREA_STOCK_MARKET" -> MarketHours.KOREA_STOCK_MARKET;
            default -> {
                log.warn("Unknown market: {}. Using default KOREA_STOCK_MARKET", market);
                yield MarketHours.KOREA_STOCK_MARKET;
            }
        };
        log.info("MarketHours configured: {}", selected);
        return selected;
    }
}