package me.rgunny.marketpulse.event.medium.infrastructure.repository;

import me.rgunny.marketpulse.event.support.BaseMongoTest;
import me.rgunny.marketpulse.event.watchlist.domain.model.WatchCategory;
import me.rgunny.marketpulse.event.watchlist.domain.model.WatchTarget;
import me.rgunny.marketpulse.event.watchlist.adapter.out.persistence.WatchTargetRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.test.StepVerifier;
import org.junit.jupiter.api.BeforeEach;

/**
 * WatchTargetRepository Local MongoDB 테스트
 */
@DisplayName("WatchTargetRepository - Local MongoDB")
@EnabledIfEnvironmentVariable(named = "USE_TESTCONTAINERS", matches = "false")
class WatchTargetRepositoryLocalTest extends BaseMongoTest {

    @Autowired
    private WatchTargetRepository watchTargetRepository;
    
    @BeforeEach
    void setUp() {
        watchTargetRepository.deleteAll().block();
    }

    @Test
    @DisplayName("Local MongoDB에서 테스트가 정상 동작한다")
    void testWithLocalMongoDB() {
        // given
        WatchTarget target = WatchTarget.createCoreStock(
            "005930", "삼성전자", 1, 10, "시가총액 1위"
        );

        // when & then
        StepVerifier.create(
            watchTargetRepository.save(target)
                .then(watchTargetRepository.findBySymbol("005930"))
        )
        .expectNextMatches(saved -> 
            saved.getSymbol().equals("005930") && 
            saved.getName().equals("삼성전자")
        )
        .verifyComplete();
    }
}