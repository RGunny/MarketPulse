package me.rgunny.marketpulse.event.unit.domain.stock;

import me.rgunny.marketpulse.event.watchlist.domain.model.WatchCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WatchCategory Enum - 감시 카테고리 (unit)")
class WatchCategoryTest {

    @Test
    @DisplayName("CORE 카테고리는 핵심 종목 정보를 올바르게 반환한다")
    void givenCoreCategory_whenGetProperties_thenReturnsCorrectInfo() {
        // given
        WatchCategory category = WatchCategory.CORE;

        // when & then
        assertThat(category.getDisplayName()).isEqualTo("핵심");
        assertThat(category.getDescription()).isEqualTo("시가총액 상위, 시장 대표 종목");
        assertThat(category.getDefaultInterval()).isEqualTo(10);
        assertThat(category.getRecommendedPriority()).isEqualTo(1);
    }

    @Test
    @DisplayName("THEME 카테고리는 테마 종목 정보를 올바르게 반환한다")
    void givenThemeCategory_whenGetProperties_thenReturnsCorrectInfo() {
        // given
        WatchCategory category = WatchCategory.THEME;

        // when & then
        assertThat(category.getDisplayName()).isEqualTo("테마");
        assertThat(category.getDescription()).isEqualTo("특정 테마/업종 관련 종목");
        assertThat(category.getDefaultInterval()).isEqualTo(30);
        assertThat(category.getRecommendedPriority()).isEqualTo(5);
    }

    @Test
    @DisplayName("MOMENTUM 카테고리는 모멘텀 종목 정보를 올바르게 반환한다")
    void givenMomentumCategory_whenGetProperties_thenReturnsCorrectInfo() {
        // given
        WatchCategory category = WatchCategory.MOMENTUM;

        // when & then
        assertThat(category.getDisplayName()).isEqualTo("모멘텀");
        assertThat(category.getDescription()).isEqualTo("급등락, 뉴스 등 단기 이슈 종목");
        assertThat(category.getDefaultInterval()).isEqualTo(20);
        assertThat(category.getRecommendedPriority()).isEqualTo(8);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    @DisplayName("CORE 카테고리는 우선순위 1-3을 유효하다고 판단한다")
    void givenCoreCategory_whenValidPriority_thenReturnsTrue(int priority) {
        // given
        WatchCategory category = WatchCategory.CORE;

        // when
        boolean isValid = category.isValidPriority(priority);

        // then
        assertThat(isValid).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 4, 5, 10})
    @DisplayName("CORE 카테고리는 우선순위 범위 밖을 무효하다고 판단한다")
    void givenCoreCategory_whenInvalidPriority_thenReturnsFalse(int priority) {
        // given
        WatchCategory category = WatchCategory.CORE;

        // when
        boolean isValid = category.isValidPriority(priority);

        // then
        assertThat(isValid).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 5, 6, 7})
    @DisplayName("THEME 카테고리는 우선순위 4-7을 유효하다고 판단한다")
    void givenThemeCategory_whenValidPriority_thenReturnsTrue(int priority) {
        // given
        WatchCategory category = WatchCategory.THEME;

        // when
        boolean isValid = category.isValidPriority(priority);

        // then
        assertThat(isValid).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {8, 9, 10})
    @DisplayName("MOMENTUM 카테고리는 우선순위 8-10을 유효하다고 판단한다")
    void givenMomentumCategory_whenValidPriority_thenReturnsTrue(int priority) {
        // given
        WatchCategory category = WatchCategory.MOMENTUM;

        // when
        boolean isValid = category.isValidPriority(priority);

        // then
        assertThat(isValid).isTrue();
    }

    @ParameterizedTest
    @EnumSource(WatchCategory.class)
    @DisplayName("모든 카테고리는 권장 우선순위가 유효 범위 내에 있다")
    void givenAllCategories_whenGetRecommendedPriority_thenWithinValidRange(WatchCategory category) {
        // given
        int recommendedPriority = category.getRecommendedPriority();

        // when
        boolean isValid = category.isValidPriority(recommendedPriority);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("카테고리별 기본 수집 주기가 적절하게 설정되어 있다")
    void givenAllCategories_whenGetDefaultInterval_thenAppropriateIntervals() {
        // given & when & then
        assertThat(WatchCategory.CORE.getDefaultInterval())
            .isLessThan(WatchCategory.MOMENTUM.getDefaultInterval())
            .isLessThan(WatchCategory.THEME.getDefaultInterval());
    }

    @Test
    @DisplayName("카테고리별 권장 우선순위가 적절한 순서로 설정되어 있다")
    void givenAllCategories_whenGetRecommendedPriority_thenAppropriateOrder() {
        // given & when & then
        assertThat(WatchCategory.CORE.getRecommendedPriority())
            .isLessThan(WatchCategory.THEME.getRecommendedPriority())
            .isLessThan(WatchCategory.MOMENTUM.getRecommendedPriority());
    }
}