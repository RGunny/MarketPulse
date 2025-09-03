package me.rgunny.marketpulse.watchlist.application.out;

import jakarta.persistence.EntityManager;
import me.rgunny.marketpulse.watchlist.domain.WatchTarget;
import me.rgunny.marketpulse.watchlist.domain.WatchTargetFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class WatchTargetRepositoryTest {

    @Autowired
    private WatchTargetRepository watchTargetRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("WatchTarget을 신규등록한다.")
    void registerWatchTarget() {
        var watchTarget = WatchTarget.register(WatchTargetFixture.registerWatchTargetRequest());

        watchTargetRepository.save(watchTarget);

        entityManager.flush();
        entityManager.clear();

        var found = watchTargetRepository.findById(watchTarget.getId()).get();

        assertEquals(watchTarget.getId(), found.getId());
        assertEquals(watchTarget.getStockCode(), found.getStockCode());
        assertEquals(watchTarget.getStockName(), found.getStockName());
    }

}