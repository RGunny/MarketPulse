package me.rgunny.marketpulse.event.shared.domain.value;

import java.math.BigDecimal;

/**
 * 비즈니스 로직에서 사용하는 상수들을 정의
 */
public final class BusinessConstants {
    
    // 우선순위 관련 상수
    public static final int HIGH_PRIORITY_THRESHOLD = 3;
    public static final int DEFAULT_PRIORITY = 1;
    public static final int MIN_PRIORITY = 1;
    public static final int MAX_PRIORITY = 10;
    
    // 수집 주기 관련 상수 (초 단위)
    public static final int DEFAULT_COLLECT_INTERVAL_SECONDS = 30;
    public static final int MIN_COLLECT_INTERVAL_SECONDS = 5;
    public static final int MAX_COLLECT_INTERVAL_SECONDS = 3600; // 1시간
    
    // 수학적 계산 상수
    public static final int CALCULATION_DECIMAL_PLACES = 4;
    public static final BigDecimal PERCENTAGE_MULTIPLIER = new BigDecimal("100");
    public static final BigDecimal ZERO = BigDecimal.ZERO;
    
    // 문자열 관련 상수
    public static final String DEFAULT_DESCRIPTION = "시스템 생성";
    public static final String TEST_DESCRIPTION = "테스트용";
    
    // 시간 관련 상수
    public static final long DEFAULT_TIMEOUT_SECONDS = 30L;
    public static final long CACHE_TTL_SECONDS = 300L; // 5분
    
    // 배치 처리 관련 상수
    public static final int DEFAULT_BATCH_SIZE = 100;
    public static final int MAX_CONCURRENT_REQUESTS = 10;
    
    private BusinessConstants() {
        // 인스턴스 생성 방지
        throw new AssertionError("Cannot instantiate constants class");
    }
}