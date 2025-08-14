package me.rgunny.marketpulse.event.medium.infrastructure.repository;

import me.rgunny.marketpulse.event.watchlist.domain.model.WatchCategory;
import me.rgunny.marketpulse.event.watchlist.domain.model.WatchTarget;
import me.rgunny.marketpulse.event.watchlist.infrastructure.adapter.out.persistence.WatchTargetRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;
import org.junit.jupiter.api.BeforeEach;

import java.time.Duration;

@DataMongoTest
@ActiveProfiles("test")
@Testcontainers
@DisplayName("WatchTargetRepository - MongoDB 연동 (medium)")
class WatchTargetRepositoryTest {

    @Autowired
    private WatchTargetRepository watchTargetRepository;
    
    @BeforeEach
    void setUp() {
        // 각 테스트 전에 컬렉션 정리
        watchTargetRepository.deleteAll().block();
    }

    @Test
    @DisplayName("활성화된 감시 대상만 조회한다")
    void givenActiveAndInactiveTargets_whenFindByActiveTrue_thenReturnsOnlyActive() {
        // given
        WatchTarget activeTarget = WatchTarget.createCoreStock(
            "005930", "삼성전자", 1, 10, "시가총액 1위"
        );
        WatchTarget inactiveTarget = WatchTarget.createCoreStock(
            "000660", "SK하이닉스", 2, 10, "시가총액 2위"
        ).deactivate();

        // when
        StepVerifier.create(
            watchTargetRepository.saveAll(java.util.List.of(activeTarget, inactiveTarget))
                .then(watchTargetRepository.findByActiveTrue().collectList())
        )
        // then
        .expectNextMatches(targets -> 
            targets.size() == 1 && 
            targets.get(0).getSymbol().equals("005930") &&
            targets.get(0).isActive()
        )
        .verifyComplete();
    }

    @Test
    @DisplayName("카테고리별 활성화된 감시 대상을 우선순위 순으로 조회한다")
    void givenMixedCategoryTargets_whenFindByCategoryAndActive_thenReturnsOrderedByPriority() {
        // given
        WatchTarget coreTarget1 = WatchTarget.createCoreStock("005930", "삼성전자", 1, 10, "시가총액 1위");
        WatchTarget coreTarget2 = WatchTarget.createCoreStock("000660", "SK하이닉스", 2, 10, "시가총액 2위");
        WatchTarget themeTarget = WatchTarget.createThemeStock("247540", "에코프로비엠", "2차전지", 5, 30, "2차전지");

        // when
        StepVerifier.create(
            watchTargetRepository.saveAll(java.util.List.of(coreTarget2, coreTarget1, themeTarget))
                .then(watchTargetRepository.findByCategoryAndActiveTrueOrderByPriorityAsc(WatchCategory.CORE)
                    .collectList())
        )
        // then
        .expectNextMatches(targets -> 
            targets.size() == 2 &&
            targets.get(0).getSymbol().equals("005930") && targets.get(0).getPriority() == 1 &&
            targets.get(1).getSymbol().equals("000660") && targets.get(1).getPriority() == 2
        )
        .verifyComplete();
    }

    @Test
    @DisplayName("종목코드로 감시 대상을 조회한다")
    void givenStoredTarget_whenFindBySymbol_thenReturnsMatchingTarget() {
        // given
        WatchTarget target = WatchTarget.createCoreStock("005930", "삼성전자", 1, 10, "시가총액 1위");

        // when
        StepVerifier.create(
            watchTargetRepository.save(target)
                .then(watchTargetRepository.findBySymbol("005930"))
        )
        // then
        .expectNextMatches(foundTarget -> 
            foundTarget.getSymbol().equals("005930") &&
            foundTarget.getName().equals("삼성전자") &&
            foundTarget.getCategory() == WatchCategory.CORE
        )
        .verifyComplete();
    }

    @Test
    @DisplayName("존재하지 않는 종목코드 조회 시 빈 결과를 반환한다")
    void givenNonExistentSymbol_whenFindBySymbol_thenReturnsEmpty() {
        // given
        String nonExistentSymbol = "999999";

        // when & then
        StepVerifier.create(watchTargetRepository.findBySymbol(nonExistentSymbol))
            .expectComplete()
            .verify(Duration.ofSeconds(5));
    }

    @Test
    @DisplayName("우선순위 범위로 활성화된 감시 대상을 조회한다")
    void givenVariousPriorityTargets_whenFindByPriorityRange_thenReturnsWithinRange() {
        // given
        WatchTarget highPriority = WatchTarget.createCoreStock("005930", "삼성전자", 2, 10, "높은 우선순위");
        WatchTarget mediumPriority = WatchTarget.createThemeStock("247540", "에코프로비엠", "2차전지", 4, 30, "중간 우선순위");
        WatchTarget lowPriority = WatchTarget.createMomentumStock("123456", "급등주", 9, 20, "낮은 우선순위");

        // when
        StepVerifier.create(
            watchTargetRepository.saveAll(java.util.List.of(highPriority, mediumPriority, lowPriority))
                .then(watchTargetRepository.findByPriorityBetweenAndActiveTrueOrderByPriorityAsc(1, 5)
                    .collectList())
        )
        // then
        .expectNextMatches(targets -> 
            targets.size() == 2 &&
            targets.stream().allMatch(target -> target.getPriority() >= 1 && target.getPriority() <= 5) &&
            targets.get(0).getPriority() < targets.get(1).getPriority()
        )
        .verifyComplete();
    }

    @Test
    @DisplayName("특정 수집 주기 이하의 활성화된 감시 대상을 조회한다")
    void givenVariousIntervalTargets_whenFindByMaxInterval_thenReturnsWithinInterval() {
        // given
        WatchTarget shortInterval = WatchTarget.createCoreStock("005930", "삼성전자", 1, 10, "짧은 주기");
        WatchTarget mediumInterval = WatchTarget.createThemeStock("247540", "에코프로비엠", "2차전지", 5, 25, "중간 주기");
        WatchTarget longInterval = WatchTarget.createMomentumStock("123456", "급등주", 8, 60, "긴 주기");

        // when
        StepVerifier.create(
            watchTargetRepository.saveAll(java.util.List.of(shortInterval, mediumInterval, longInterval))
                .then(watchTargetRepository.findByCollectIntervalLessThanEqualAndActiveTrueOrderByPriorityAsc(30)
                    .collectList())
        )
        // then
        .expectNextMatches(targets -> 
            targets.size() == 2 &&
            targets.stream().allMatch(target -> target.getCollectInterval() <= 30) &&
            targets.get(0).getPriority() < targets.get(1).getPriority()
        )
        .verifyComplete();
    }

    @Test
    @DisplayName("테마별 활성화된 감시 대상을 우선순위 순으로 조회한다")
    void givenSameThemeTargets_whenFindByTheme_thenReturnsOrderedByPriority() {
        // given
        String theme = "2차전지";
        WatchTarget target1 = WatchTarget.createThemeStock("247540", "에코프로비엠", theme, 5, 30, "대표 종목");
        WatchTarget target2 = WatchTarget.createThemeStock("086520", "에코프로", theme, 4, 30, "관련 종목");
        WatchTarget otherTheme = WatchTarget.createThemeStock("005930", "삼성전자", "반도체", 1, 10, "다른 테마");

        // when
        StepVerifier.create(
            watchTargetRepository.saveAll(java.util.List.of(target1, target2, otherTheme))
                .then(watchTargetRepository.findByThemeAndActiveTrueOrderByPriorityAsc(theme)
                    .collectList())
        )
        // then
        .expectNextMatches(targets -> 
            targets.size() == 2 &&
            targets.stream().allMatch(target -> theme.equals(target.getTheme())) &&
            targets.get(0).getPriority() < targets.get(1).getPriority()
        )
        .verifyComplete();
    }

    @Test
    @DisplayName("비활성화된 감시 대상은 조회 결과에 포함되지 않는다")
    void givenInactiveTargets_whenFindActiveTargets_thenExcludesInactive() {
        // given
        WatchTarget activeTarget = WatchTarget.createCoreStock("005930", "삼성전자", 1, 10, "활성");
        WatchTarget inactiveTarget = WatchTarget.createCoreStock("000660", "SK하이닉스", 2, 10, "비활성").deactivate();

        // when
        StepVerifier.create(
            watchTargetRepository.saveAll(java.util.List.of(activeTarget, inactiveTarget))
                .then(watchTargetRepository.findByCategoryAndActiveTrueOrderByPriorityAsc(WatchCategory.CORE)
                    .collectList())
        )
        // then
        .expectNextMatches(targets -> 
            targets.size() == 1 &&
            targets.get(0).isActive() &&
            targets.get(0).getSymbol().equals("005930")
        )
        .verifyComplete();
    }

    @Test
    @DisplayName("MongoDB에 WatchTarget이 올바르게 저장되고 조회된다")
    void givenWatchTarget_whenSaveAndFind_thenDataPersistsCorrectly() {
        // given
        WatchTarget originalTarget = WatchTarget.createThemeStock(
            "247540", "에코프로비엠", "2차전지", 5, 30, "2차전지 대표 종목"
        );

        // when
        StepVerifier.create(
            watchTargetRepository.save(originalTarget)
                .then(watchTargetRepository.findBySymbol("247540"))
        )
        // then
        .expectNextMatches(savedTarget -> 
            savedTarget.getSymbol().equals(originalTarget.getSymbol()) &&
            savedTarget.getName().equals(originalTarget.getName()) &&
            savedTarget.getCategory() == originalTarget.getCategory() &&
            savedTarget.getTheme().equals(originalTarget.getTheme()) &&
            savedTarget.getPriority() == originalTarget.getPriority() &&
            savedTarget.getCollectInterval() == originalTarget.getCollectInterval() &&
            savedTarget.isActive() == originalTarget.isActive() &&
            savedTarget.getReason().equals(originalTarget.getReason())
        )
        .verifyComplete();
    }
}