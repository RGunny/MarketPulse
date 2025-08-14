package me.rgunny.marketpulse.event.marketdata.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * 종목 동기화 결과
 */
public record SyncResult(
        SyncMode syncMode,
        Instant startTime,
        Instant endTime,
        int totalProcessed,
        int successCount,
        int failedCount,
        int newCount,
        int updatedCount,
        List<String> errors,
        SyncStatus status
) {
    
    /**
     * 성공적인 동기화 결과 생성
     */
    public static SyncResult success(
            SyncMode syncMode,
            Instant startTime,
            int totalProcessed,
            int newCount,
            int updatedCount
    ) {
        return new SyncResult(
                syncMode,
                startTime,
                Instant.now(),
                totalProcessed,
                totalProcessed,
                0,
                newCount,
                updatedCount,
                List.of(),
                SyncStatus.SUCCESS
        );
    }
    
    /**
     * 실패한 동기화 결과 생성
     */
    public static SyncResult failure(
            SyncMode syncMode,
            Instant startTime,
            List<String> errors
    ) {
        return new SyncResult(
                syncMode,
                startTime,
                Instant.now(),
                0,
                0,
                0,
                0,
                0,
                errors,
                SyncStatus.FAILED
        );
    }
    
    /**
     * 부분 성공 동기화 결과 생성
     */
    public static SyncResult partial(
            SyncMode syncMode,
            Instant startTime,
            int totalProcessed,
            int successCount,
            int failedCount,
            int newCount,
            int updatedCount,
            List<String> errors
    ) {
        return new SyncResult(
                syncMode,
                startTime,
                Instant.now(),
                totalProcessed,
                successCount,
                failedCount,
                newCount,
                updatedCount,
                errors,
                SyncStatus.PARTIAL
        );
    }
    
    /**
     * 동기화 소요 시간
     */
    public Duration getDuration() {
        return Duration.between(startTime, endTime);
    }
    
    /**
     * 동기화 소요 시간 (초)
     */
    public long getDurationSeconds() {
        return getDuration().getSeconds();
    }
    
    /**
     * 성공률
     */
    public double getSuccessRate() {
        if (totalProcessed == 0) return 0.0;
        return (double) successCount / totalProcessed * 100;
    }
}