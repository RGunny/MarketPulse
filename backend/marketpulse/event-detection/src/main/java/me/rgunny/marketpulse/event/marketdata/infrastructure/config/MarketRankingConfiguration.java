package me.rgunny.marketpulse.event.marketdata.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 시장 순위 수집 설정 활성화
 */
@Configuration
@EnableConfigurationProperties(MarketRankingProperties.class)
public class MarketRankingConfiguration {
    // Properties 빈 자동 등록
}