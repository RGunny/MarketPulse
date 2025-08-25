package me.rgunny.marketpulse.event.marketdata.infrastructure.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * 시장 순위 수집 설정
 */
@Validated
@ConfigurationProperties(prefix = "marketpulse.market-ranking")
public record MarketRankingProperties(
        boolean enabled,
        @NotNull AutoWatch autoWatch,
        @NotNull Schedule schedule,
        @Min(1) int retentionDays
) {
    
    public MarketRankingProperties {
        if (retentionDays <= 0) {
            retentionDays = 7; // 기본값
        }
    }
    
    /**
     * 자동 WatchTarget 등록 설정
     */
    public record AutoWatch(
            boolean enabled,
            @Min(1) int priceRiseLimit,
            @Min(1) int priceFallLimit,
            @Min(1) int volumeSurgeLimit,
            @Min(1) int priority,
            @Min(10) int collectIntervalSeconds
    ) {
        public AutoWatch {
            if (priceRiseLimit <= 0) priceRiseLimit = 10;
            if (priceFallLimit <= 0) priceFallLimit = 10;
            if (volumeSurgeLimit <= 0) volumeSurgeLimit = 5;
            if (priority <= 0) priority = 2;
            if (collectIntervalSeconds <= 0) collectIntervalSeconds = 60;
        }
    }
    
    /**
     * 스케줄 설정
     */
    public record Schedule(
            String kospiCron,
            String kosdaqCron,
            String anomalyCron,
            String morningCron,
            String cleanupCron
    ) {
        public Schedule {
            // 기본 스케줄 설정
            if (kospiCron == null || kospiCron.isBlank()) {
                kospiCron = "0 */10 9-15 * * MON-FRI";
            }
            if (kosdaqCron == null || kosdaqCron.isBlank()) {
                kosdaqCron = "0 5-55/10 9-15 * * MON-FRI";
            }
            if (anomalyCron == null || anomalyCron.isBlank()) {
                anomalyCron = "0 */5 9-15 * * MON-FRI";
            }
            if (morningCron == null || morningCron.isBlank()) {
                morningCron = "0 1 9 * * MON-FRI";
            }
            if (cleanupCron == null || cleanupCron.isBlank()) {
                cleanupCron = "0 0 3 * * *";
            }
        }
    }
}