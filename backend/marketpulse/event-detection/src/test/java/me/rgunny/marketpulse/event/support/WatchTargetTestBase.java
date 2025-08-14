package me.rgunny.marketpulse.event.support;

import me.rgunny.marketpulse.event.shared.domain.value.BusinessConstants;
import me.rgunny.marketpulse.event.watchlist.domain.model.WatchCategory;
import me.rgunny.marketpulse.event.watchlist.domain.model.WatchTarget;

import java.time.LocalDateTime;

/**
 * WatchTarget 테스트를 위한 공통 베이스 클래스
 */
public abstract class WatchTargetTestBase {
    
    /**
     * 기본 WatchTarget 생성 (삼성전자, CORE 카테고리, 우선순위 1, 30초 주기)
     */
    protected WatchTarget createDefaultWatchTarget() {
        return createWatchTarget("005930", "삼성전자");
    }
    
    /**
     * 기본 설정으로 WatchTarget 생성
     */
    protected WatchTarget createWatchTarget(String symbol, String name) {
        return createWatchTarget(symbol, name, WatchCategory.CORE, 
                                BusinessConstants.DEFAULT_PRIORITY, 
                                BusinessConstants.DEFAULT_COLLECT_INTERVAL_SECONDS);
    }
    
    /**
     * 카테고리 지정 WatchTarget 생성
     */
    protected WatchTarget createWatchTarget(String symbol, String name, WatchCategory category) {
        return createWatchTarget(symbol, name, category, 
                                BusinessConstants.DEFAULT_PRIORITY, 
                                BusinessConstants.DEFAULT_COLLECT_INTERVAL_SECONDS);
    }
    
    /**
     * 수집 주기 지정 WatchTarget 생성
     */
    protected WatchTarget createWatchTarget(String symbol, String name, int collectInterval) {
        return createWatchTarget(symbol, name, WatchCategory.CORE, 
                                BusinessConstants.DEFAULT_PRIORITY, 
                                collectInterval);
    }
    
    /**
     * 우선순위 지정 WatchTarget 생성
     */
    protected WatchTarget createWatchTarget(String symbol, String name, int priority, int collectInterval) {
        return createWatchTarget(symbol, name, WatchCategory.CORE, priority, collectInterval);
    }
    
    /**
     * 카테고리와 수집 주기 지정 WatchTarget 생성
     */
    protected WatchTarget createWatchTarget(String symbol, String name, WatchCategory category, int collectInterval) {
        return createWatchTarget(symbol, name, category, 
                                BusinessConstants.DEFAULT_PRIORITY, 
                                collectInterval);
    }
    
    /**
     * 모든 속성을 지정하여 WatchTarget 생성
     */
    protected WatchTarget createWatchTarget(String symbol, String name, WatchCategory category, 
                                          int priority, int collectInterval) {
        return createWatchTarget(symbol, name, category, priority, collectInterval, true);
    }
    
    /**
     * 활성화 상태를 포함한 완전한 WatchTarget 생성
     */
    protected WatchTarget createWatchTarget(String symbol, String name, WatchCategory category, 
                                          int priority, int collectInterval, boolean active) {
        LocalDateTime now = LocalDateTime.now();
        return new WatchTarget(
                null, // ID는 MongoDB가 자동 생성
                symbol, 
                name, 
                category, 
                null, // 설명은 기본값 사용
                priority, 
                collectInterval, 
                active, 
                BusinessConstants.TEST_DESCRIPTION,
                now, 
                now
        );
    }
    
    /**
     * 높은 우선순위 WatchTarget 생성
     */
    protected WatchTarget createHighPriorityWatchTarget(String symbol, String name) {
        return createWatchTarget(symbol, name, WatchCategory.CORE, 1, 15);
    }
    
    /**
     * 낮은 우선순위 WatchTarget 생성
     */
    protected WatchTarget createLowPriorityWatchTarget(String symbol, String name) {
        return createWatchTarget(symbol, name, WatchCategory.MOMENTUM, 5, 60);
    }
    
    /**
     * 비활성화된 WatchTarget 생성
     */
    protected WatchTarget createInactiveWatchTarget(String symbol, String name) {
        return createWatchTarget(symbol, name, WatchCategory.CORE, 
                                BusinessConstants.DEFAULT_PRIORITY, 
                                BusinessConstants.DEFAULT_COLLECT_INTERVAL_SECONDS, false);
    }
}