package me.rgunny.event.unit.domain.stock;

import me.rgunny.event.watchlist.domain.model.WatchCategory;
import me.rgunny.event.watchlist.domain.model.WatchTarget;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WatchTarget 도메인 엔티티 - 감시 대상 관리 (unit)")
class WatchTargetTest {

    @Test
    @DisplayName("핵심 종목 생성 시 CORE 카테고리와 높은 우선순위가 설정된다")
    void givenCoreStockData_whenCreateCoreStock_thenCoreCategory() {
        // given
        String symbol = "005930";
        String name = "삼성전자";
        int priority = 1;
        int collectInterval = 10;
        String reason = "시가총액 1위";

        // when
        WatchTarget watchTarget = WatchTarget.createCoreStock(symbol, name, priority, collectInterval, reason);

        // then
        assertThat(watchTarget.getSymbol()).isEqualTo(symbol);
        assertThat(watchTarget.getName()).isEqualTo(name);
        assertThat(watchTarget.getCategory()).isEqualTo(WatchCategory.CORE);
        assertThat(watchTarget.getPriority()).isEqualTo(priority);
        assertThat(watchTarget.getCollectInterval()).isEqualTo(collectInterval);
        assertThat(watchTarget.isActive()).isTrue();
        assertThat(watchTarget.getReason()).isEqualTo(reason);
        assertThat(watchTarget.getCreatedAt()).isNotNull();
        assertThat(watchTarget.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("테마 종목 생성 시 THEME 카테고리와 테마 정보가 설정된다")
    void givenThemeStockData_whenCreateThemeStock_thenThemeCategoryWithTheme() {
        // given
        String symbol = "247540";
        String name = "에코프로비엠";
        String theme = "2차전지";
        int priority = 5;
        int collectInterval = 30;
        String reason = "2차전지 대표 종목";

        // when
        WatchTarget watchTarget = WatchTarget.createThemeStock(symbol, name, theme, priority, collectInterval, reason);

        // then
        assertThat(watchTarget.getSymbol()).isEqualTo(symbol);
        assertThat(watchTarget.getName()).isEqualTo(name);
        assertThat(watchTarget.getCategory()).isEqualTo(WatchCategory.THEME);
        assertThat(watchTarget.getTheme()).isEqualTo(theme);
        assertThat(watchTarget.getPriority()).isEqualTo(priority);
        assertThat(watchTarget.getCollectInterval()).isEqualTo(collectInterval);
        assertThat(watchTarget.isActive()).isTrue();
    }

    @Test
    @DisplayName("모멘텀 종목 생성 시 MOMENTUM 카테고리가 설정된다")
    void givenMomentumStockData_whenCreateMomentumStock_thenMomentumCategory() {
        // given
        String symbol = "123456";
        String name = "급등주";
        int priority = 8;
        int collectInterval = 20;
        String reason = "전일 +15% 급등";

        // when
        WatchTarget watchTarget = WatchTarget.createMomentumStock(symbol, name, priority, collectInterval, reason);

        // then
        assertThat(watchTarget.getSymbol()).isEqualTo(symbol);
        assertThat(watchTarget.getName()).isEqualTo(name);
        assertThat(watchTarget.getCategory()).isEqualTo(WatchCategory.MOMENTUM);
        assertThat(watchTarget.getTheme()).isNull();
        assertThat(watchTarget.getPriority()).isEqualTo(priority);
        assertThat(watchTarget.getCollectInterval()).isEqualTo(collectInterval);
        assertThat(watchTarget.isActive()).isTrue();
    }

    @Test
    @DisplayName("우선순위가 3 이하인 경우 고우선순위로 판단한다")
    void givenHighPriorityValue_whenCheckHighPriority_thenReturnsTrue() {
        // given
        WatchTarget highPriorityTarget = WatchTarget.createCoreStock(
            "005930", "삼성전자", 1, 10, "시가총액 1위"
        );
        WatchTarget mediumPriorityTarget = WatchTarget.createThemeStock(
            "247540", "에코프로비엠", "2차전지", 5, 30, "2차전지 대표"
        );

        // when
        boolean isHighPriority1 = highPriorityTarget.isHighPriority();
        boolean isHighPriority2 = mediumPriorityTarget.isHighPriority();

        // then
        assertThat(isHighPriority1).isTrue();
        assertThat(isHighPriority2).isFalse();
    }

    @Test
    @DisplayName("수집 주기가 지난 경우 수집이 필요하다고 판단한다")
    void givenIntervalPassed_whenShouldCollectNow_thenReturnsTrue() {
        // given
        WatchTarget watchTarget = WatchTarget.createCoreStock(
            "005930", "삼성전자", 1, 10, "시가총액 1위"
        );
        LocalDateTime lastCollectionTime = LocalDateTime.now().minusSeconds(15); // 15초 전

        // when
        boolean shouldCollect = watchTarget.shouldCollectNow(lastCollectionTime);

        // then
        assertThat(shouldCollect).isTrue();
    }

    @Test
    @DisplayName("수집 주기가 아직 안된 경우 수집이 필요없다고 판단한다")
    void givenIntervalNotPassed_whenShouldCollectNow_thenReturnsFalse() {
        // given
        WatchTarget watchTarget = WatchTarget.createCoreStock(
            "005930", "삼성전자", 1, 10, "시가총액 1위"
        );
        LocalDateTime lastCollectionTime = LocalDateTime.now().minusSeconds(5); // 5초 전

        // when
        boolean shouldCollect = watchTarget.shouldCollectNow(lastCollectionTime);

        // then
        assertThat(shouldCollect).isFalse();
    }

    @Test
    @DisplayName("마지막 수집 시간이 null인 경우 즉시 수집이 필요하다고 판단한다")
    void givenNullLastCollectionTime_whenShouldCollectNow_thenReturnsTrue() {
        // given
        WatchTarget watchTarget = WatchTarget.createCoreStock(
            "005930", "삼성전자", 1, 10, "시가총액 1위"
        );
        LocalDateTime lastCollectionTime = null;

        // when
        boolean shouldCollect = watchTarget.shouldCollectNow(lastCollectionTime);

        // then
        assertThat(shouldCollect).isTrue();
    }

    @Test
    @DisplayName("활성화 시 새로운 불변 인스턴스를 반환하고 활성 상태가 된다")
    void givenInactiveTarget_whenActivate_thenReturnsNewActiveInstance() {
        // given
        WatchTarget inactiveTarget = WatchTarget.createCoreStock(
            "005930", "삼성전자", 1, 10, "시가총액 1위"
        ).deactivate();

        // when
        WatchTarget activatedTarget = inactiveTarget.activate();

        // then
        assertThat(activatedTarget).isNotSameAs(inactiveTarget);
        assertThat(activatedTarget.isActive()).isTrue();
        assertThat(inactiveTarget.isActive()).isFalse();
        assertThat(activatedTarget.getSymbol()).isEqualTo(inactiveTarget.getSymbol());
        assertThat(activatedTarget.getUpdatedAt()).isAfter(inactiveTarget.getUpdatedAt());
    }

    @Test
    @DisplayName("비활성화 시 새로운 불변 인스턴스를 반환하고 비활성 상태가 된다")
    void givenActiveTarget_whenDeactivate_thenReturnsNewInactiveInstance() {
        // given
        WatchTarget activeTarget = WatchTarget.createCoreStock(
            "005930", "삼성전자", 1, 10, "시가총액 1위"
        );

        // when
        WatchTarget deactivatedTarget = activeTarget.deactivate();

        // then
        assertThat(deactivatedTarget).isNotSameAs(activeTarget);
        assertThat(deactivatedTarget.isActive()).isFalse();
        assertThat(activeTarget.isActive()).isTrue();
        assertThat(deactivatedTarget.getSymbol()).isEqualTo(activeTarget.getSymbol());
        assertThat(deactivatedTarget.getUpdatedAt()).isAfter(activeTarget.getUpdatedAt());
    }

    @Test
    @DisplayName("수집 주기 변경 시 새로운 불변 인스턴스를 반환하고 주기가 업데이트된다")
    void givenExistingTarget_whenUpdateCollectInterval_thenReturnsNewInstanceWithUpdatedInterval() {
        // given
        WatchTarget originalTarget = WatchTarget.createCoreStock(
            "005930", "삼성전자", 1, 10, "시가총액 1위"
        );
        int newInterval = 5;

        // when
        WatchTarget updatedTarget = originalTarget.updateCollectInterval(newInterval);

        // then
        assertThat(updatedTarget).isNotSameAs(originalTarget);
        assertThat(updatedTarget.getCollectInterval()).isEqualTo(newInterval);
        assertThat(originalTarget.getCollectInterval()).isEqualTo(10);
        assertThat(updatedTarget.getSymbol()).isEqualTo(originalTarget.getSymbol());
        assertThat(updatedTarget.getUpdatedAt()).isAfter(originalTarget.getUpdatedAt());
    }

    @Test
    @DisplayName("비활성 상태인 감시 대상은 수집하지 않는다")
    void givenInactiveTarget_whenShouldCollectNow_thenReturnsFalse() {
        // given
        WatchTarget inactiveTarget = WatchTarget.createCoreStock(
            "005930", "삼성전자", 1, 10, "시가총액 1위"
        ).deactivate();
        LocalDateTime lastCollectionTime = LocalDateTime.now().minusSeconds(15);

        // when
        boolean shouldCollect = inactiveTarget.shouldCollectNow(lastCollectionTime);

        // then
        assertThat(shouldCollect).isFalse();
    }

    @Test
    @DisplayName("모든 팩토리 메서드는 createdAt과 updatedAt을 현재 시간으로 설정한다")
    void givenFactoryMethods_whenCreateTarget_thenTimestampsAreSet() {
        // given
        LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);

        // when
        WatchTarget coreTarget = WatchTarget.createCoreStock("005930", "삼성전자", 1, 10, "시가총액 1위");
        WatchTarget themeTarget = WatchTarget.createThemeStock("247540", "에코프로비엠", "2차전지", 5, 30, "2차전지");
        WatchTarget momentumTarget = WatchTarget.createMomentumStock("123456", "급등주", 8, 20, "급등");

        LocalDateTime afterCreation = LocalDateTime.now().plusSeconds(1);

        // then
        assertThat(coreTarget.getCreatedAt()).isBetween(beforeCreation, afterCreation);
        assertThat(coreTarget.getUpdatedAt()).isBetween(beforeCreation, afterCreation);
        assertThat(themeTarget.getCreatedAt()).isBetween(beforeCreation, afterCreation);
        assertThat(themeTarget.getUpdatedAt()).isBetween(beforeCreation, afterCreation);
        assertThat(momentumTarget.getCreatedAt()).isBetween(beforeCreation, afterCreation);
        assertThat(momentumTarget.getUpdatedAt()).isBetween(beforeCreation, afterCreation);
    }
}