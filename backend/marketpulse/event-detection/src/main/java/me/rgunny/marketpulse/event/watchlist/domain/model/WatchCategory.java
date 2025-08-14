package me.rgunny.marketpulse.event.watchlist.domain.model;

/**
 * 감시 종목 카테고리
 * - CORE: 핵심 관찰 종목 (시가총액 TOP, 대표 종목)
 * - THEME: 테마 종목 (AI, 2차전지, 바이오 등)
 * - MOMENTUM: 모멘텀 종목 (급등락, 뉴스 언급 등)
 */
public enum WatchCategory {
    
    CORE("핵심", "시가총액 상위, 시장 대표 종목", 10),
    THEME("테마", "특정 테마/업종 관련 종목", 30), 
    MOMENTUM("모멘텀", "급등락, 뉴스 등 단기 이슈 종목", 20);
    
    private final String displayName;
    private final String description;
    private final int defaultInterval;  // 기본 수집 주기(초)
    
    WatchCategory(String displayName, String description, int defaultInterval) {
        this.displayName = displayName;
        this.description = description;
        this.defaultInterval = defaultInterval;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getDefaultInterval() {
        return defaultInterval;
    }
    
    // 카테고리별 우선순위 범위
    public boolean isValidPriority(int priority) {
        return switch (this) {
            case CORE -> priority >= 1 && priority <= 3;      // 최고 우선순위
            case THEME -> priority >= 4 && priority <= 7;     // 중간 우선순위
            case MOMENTUM -> priority >= 8 && priority <= 10; // 낮은 우선순위
        };
    }
    
    // 카테고리별 권장 우선순위
    public int getRecommendedPriority() {
        return switch (this) {
            case CORE -> 1;
            case THEME -> 5;
            case MOMENTUM -> 8;
        };
    }
}