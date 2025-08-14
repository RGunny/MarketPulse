package me.rgunny.marketpulse.event.marketdata.infrastructure.config.shared;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 주식 시세 수집 관련 설정 프로퍼티
 */
@ConfigurationProperties(prefix = "app.stock-collection")
public record StockCollectionProperties(
        /**
         * 동시 처리 제한 설정
         */
        Concurrency concurrency,
        
        /**
         * 스케줄링 주기 설정
         */
        Schedule schedule,
        
        /**
         * 타임아웃 설정
         */
        Timeout timeout,
        
        /**
         * 우선순위 범위 설정
         */
        Priority priority,
        
        /**
         * 메모리 관리 설정
         */
        Memory memory
) {
    
    public StockCollectionProperties {
        if (concurrency == null) {
            concurrency = new Concurrency(0, 0, 0);
        }
        if (schedule == null) {
            schedule = new Schedule(null, null, null, null, null);
        }
        if (timeout == null) {
            timeout = new Timeout(null, null, null);
        }
        if (priority == null) {
            priority = new Priority(0, 0, null);
        }
        if (memory == null) {
            memory = new Memory(null, null);
        }
    }
    
    public record Concurrency(
            /**
             * 기본 동시 처리 제한
             */
            int defaultLimit,
            
            /**
             * 높은 우선순위 동시 처리 제한
             */
            int highPriority,
            
            /**
             * 카테고리별 동시 처리 제한
             */
            int categoryLimit
    ) {
        public Concurrency {
            if (defaultLimit <= 0) defaultLimit = 10;
            if (highPriority <= 0) highPriority = 5;
            if (categoryLimit <= 0) categoryLimit = 8;
        }
    }
    
    public record Schedule(
            /**
             * 전체 활성 종목 수집 주기
             */
            Duration activeStocks,
            
            /**
             * 높은 우선순위 수집 주기
             */
            Duration highPriority,
            
            /**
             * 코어 종목 수집 cron (매 분)
             */
            String coreStocks,
            
            /**
             * 초기 지연시간
             */
            Duration initialDelay,
            
            /**
             * 높은 우선순위 초기 지연
             */
            Duration highPriorityDelay
    ) {
        public Schedule {
            if (activeStocks == null) activeStocks = Duration.ofSeconds(30);
            if (highPriority == null) highPriority = Duration.ofSeconds(15);
            if (coreStocks == null || coreStocks.isBlank()) coreStocks = "0 * * * * *";
            if (initialDelay == null) initialDelay = Duration.ofSeconds(10);
            if (highPriorityDelay == null) highPriorityDelay = Duration.ofSeconds(5);
        }
    }
    
    public record Timeout(
            /**
             * 전체 수집 타임아웃
             */
            Duration activeCollection,
            
            /**
             * 높은 우선순위 타임아웃
             */
            Duration highPriority,
            
            /**
             * 코어 종목 타임아웃
             */
            Duration coreStocks
    ) {
        public Timeout {
            if (activeCollection == null) activeCollection = Duration.ofMinutes(2);
            if (highPriority == null) highPriority = Duration.ofSeconds(45);
            if (coreStocks == null) coreStocks = Duration.ofSeconds(50);
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
            int highMax,
            
            /**
             * 코어 카테고리명
             */
            String coreCategory
    ) {
        public Priority {
            if (highMin <= 0) highMin = 1;
            if (highMax <= 0) highMax = 3;
            if (coreCategory == null || coreCategory.isBlank()) coreCategory = "CORE";
        }
    }
    
    public record Memory(
            /**
             * 메모리 정리 주기
             */
            Duration cleanupInterval,
            
            /**
             * 엔트리 TTL
             */
            Duration entryTtl
    ) {
        public Memory {
            if (cleanupInterval == null) cleanupInterval = Duration.ofHours(1);
            if (entryTtl == null) entryTtl = Duration.ofDays(1);
        }
    }
    
    /**
     * 설정 값 검증
     */
    public void validate() {
        if (concurrency.defaultLimit() <= 0) {
            throw new IllegalArgumentException("concurrency.defaultLimit must be positive");
        }
        if (schedule.activeStocks().isNegative() || schedule.activeStocks().isZero()) {
            throw new IllegalArgumentException("schedule.activeStocks must be positive");
        }
        if (priority.highMin() > priority.highMax()) {
            throw new IllegalArgumentException("priority.highMin must be <= priority.highMax");
        }
    }
}