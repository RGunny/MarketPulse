package me.rgunny.event.domain.stock;

import lombok.Builder;
import lombok.Getter;
import me.rgunny.event.domain.constants.BusinessConstants;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Builder
@Document(collection = "watch_targets")
public class WatchTarget {
    
    @Id
    private final String id;
    
    @Indexed(unique = true)
    private final String symbol;              // 종목코드
    private final String name;                // 종목명
    private final WatchCategory category;     // 감시 카테고리
    private final String theme;               // 테마 (테마 종목인 경우)
    private final int priority;               // 수집 우선순위 (1: 최고, 10: 최저)
    private final int collectInterval;        // 수집 주기(초)
    private final boolean active;             // 활성화 여부
    private final String reason;              // 선정 이유
    
    private final LocalDateTime createdAt;    // 생성일시
    private final LocalDateTime updatedAt;    // 수정일시
    
    // MongoDB 영속성을 위한 생성자
    @PersistenceCreator
    public WatchTarget(String id, String symbol, String name, WatchCategory category, 
                      String theme, int priority, int collectInterval, boolean active, 
                      String reason, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.symbol = symbol;
        this.name = name;
        this.category = category;
        this.theme = theme;
        this.priority = priority;
        this.collectInterval = collectInterval;
        this.active = active;
        this.reason = reason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // 비즈니스 메서드
    public boolean isHighPriority() {
        return priority <= BusinessConstants.HIGH_PRIORITY_THRESHOLD;
    }
    
    public boolean shouldCollectNow(LocalDateTime lastCollectionTime) {
        if (!active) {
            return false;  // 비활성 상태에서는 수집하지 않음
        }
        if (lastCollectionTime == null) {
            return true;   // 활성이면서 처음 수집인 경우
        }
        return lastCollectionTime.plusSeconds(collectInterval).isBefore(LocalDateTime.now());
    }
    
    // 활성화/비활성화 상태 변경 (불변성 유지)
    public WatchTarget activate() {
        return new WatchTarget(
            this.id, this.symbol, this.name, this.category, this.theme,
            this.priority, this.collectInterval, true, this.reason,
            this.createdAt, LocalDateTime.now()
        );
    }
    
    public WatchTarget deactivate() {
        return new WatchTarget(
            this.id, this.symbol, this.name, this.category, this.theme,
            this.priority, this.collectInterval, false, this.reason,
            this.createdAt, LocalDateTime.now()
        );
    }
    
    // 수집 주기 변경 (불변성 유지)
    public WatchTarget updateCollectInterval(int newInterval) {
        return new WatchTarget(
            this.id, this.symbol, this.name, this.category, this.theme,
            this.priority, newInterval, this.active, this.reason,
            this.createdAt, LocalDateTime.now()
        );
    }
    
    // 팩토리 메서드 (신규 생성 시 id는 null, MongoDB가 자동 생성)
    public static WatchTarget createCoreStock(String symbol, String name, int priority, int collectInterval, String reason) {
        LocalDateTime now = LocalDateTime.now();
        return new WatchTarget(
            null,  // MongoDB가 ObjectId 자동 생성
            symbol, name, WatchCategory.CORE, null,
            priority, collectInterval, true, reason,
            now, now
        );
    }
    
    public static WatchTarget createThemeStock(String symbol, String name, String theme, int priority, int collectInterval, String reason) {
        LocalDateTime now = LocalDateTime.now();
        return new WatchTarget(
            null,  // MongoDB가 ObjectId 자동 생성
            symbol, name, WatchCategory.THEME, theme,
            priority, collectInterval, true, reason,
            now, now
        );
    }
    
    public static WatchTarget createMomentumStock(String symbol, String name, int priority, int collectInterval, String reason) {
        LocalDateTime now = LocalDateTime.now();
        return new WatchTarget(
            null,  // MongoDB가 ObjectId 자동 생성
            symbol, name, WatchCategory.MOMENTUM, null,
            priority, collectInterval, true, reason,
            now, now
        );
    }
}