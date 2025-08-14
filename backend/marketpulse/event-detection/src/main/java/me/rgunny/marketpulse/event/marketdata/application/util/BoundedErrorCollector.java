package me.rgunny.marketpulse.event.marketdata.application.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Thread-Safe 제한된 에러 수집기
 * 메모리 안전성을 보장하는 에러 수집 유틸리티
 */
public class BoundedErrorCollector {
    
    private final Queue<ErrorEntry> errors = new ConcurrentLinkedQueue<>();
    private final AtomicInteger errorCount = new AtomicInteger(0);
    private final AtomicInteger droppedCount = new AtomicInteger(0);
    private final int maxSize;
    
    public BoundedErrorCollector(int maxSize) {
        this.maxSize = maxSize;
    }
    
    /**
     * 에러 추가 (Thread-Safe)
     * 
     * @param context 에러 컨텍스트 (예: 종목코드)
     * @param message 에러 메시지
     * @return 추가 성공 여부
     */
    public boolean addError(String context, String message) {
        if (errorCount.get() >= maxSize) {
            droppedCount.incrementAndGet();
            return false;
        }
        
        ErrorEntry entry = new ErrorEntry(context, message, System.currentTimeMillis());
        if (errors.offer(entry)) {
            errorCount.incrementAndGet();
            return true;
        }
        return false;
    }
    
    /**
     * 수집된 에러 목록 조회
     * 
     * @return 에러 메시지 리스트
     */
    public List<String> getErrors() {
        List<String> result = new ArrayList<>();
        for (ErrorEntry entry : errors) {
            result.add(String.format("[%s] %s", entry.context, entry.message));
        }
        return result;
    }
    
    /**
     * 상세 에러 정보 조회
     * 
     * @return 에러 엔트리 리스트
     */
    public List<ErrorEntry> getErrorEntries() {
        return new ArrayList<>(errors);
    }
    
    /**
     * 에러 통계
     * 
     * @return 에러 통계 정보
     */
    public ErrorStats getStats() {
        return new ErrorStats(errorCount.get(), droppedCount.get(), maxSize);
    }
    
    /**
     * 초기화
     */
    public void clear() {
        errors.clear();
        errorCount.set(0);
        droppedCount.set(0);
    }
    
    /**
     * 에러 엔트리
     */
    public static record ErrorEntry(
            String context,
            String message,
            long timestamp
    ) {}
    
    /**
     * 에러 통계
     */
    public static record ErrorStats(
            int collectedCount,
            int droppedCount,
            int maxSize
    ) {
        public double getDropRate() {
            int total = collectedCount + droppedCount;
            return total == 0 ? 0.0 : (double) droppedCount / total * 100;
        }
    }
}