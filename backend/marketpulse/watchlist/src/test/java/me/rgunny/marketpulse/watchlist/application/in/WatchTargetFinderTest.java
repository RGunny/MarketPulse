package me.rgunny.marketpulse.watchlist.application.in;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import me.rgunny.marketpulse.watchlist.domain.WatchTarget;
import me.rgunny.marketpulse.watchlist.domain.WatchTargetFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Transactional
//@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
record WatchTargetFinderTest(WatchTargetFinder watchTargetFinder, WatchTargetRegister watchTargetRegister,
                             EntityManager entityManager) {

    static WatchTarget watchTarget;

    @BeforeEach
    void setUp(){
        watchTarget = watchTargetRegister.register(WatchTargetFixture.registerWatchTargetRequest());
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("WatchTarget 조회를 성공한다.")
    void find() {
        WatchTarget found = watchTargetFinder.find(watchTarget.getId());

        assertThat(watchTarget.getId()).isEqualTo(found.getId());
    }


}