package me.rgunny.marketpulse.event.watchlist.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 감시 목록 관련 설정 프로퍼티
 */
@ConfigurationProperties(prefix = "app.watchlist")
public record WatchlistProperties(
        /**
         * 우선순위 범위 설정
         */
        Priority priority
) {
    
    public WatchlistProperties {
        if (priority == null) {
            priority = new Priority(0, 0);
        }
    }
    
    public record Priority(
            /**
             * 높은 우선순위 최소값
             */
            int highMin,
            
            /**
             * 높은 우선순위 최대값
             */
            int highMax
    ) {
        public Priority {
            if (highMin <= 0) highMin = 1;
            if (highMax <= 0) highMax = 3;
        }
    }
    
    /**
     * 설정 값 검증
     */
    public void validate() {
        if (priority.highMin() > priority.highMax()) {
            throw new IllegalArgumentException("priority.highMin must be <= priority.highMax");
        }
    }
}