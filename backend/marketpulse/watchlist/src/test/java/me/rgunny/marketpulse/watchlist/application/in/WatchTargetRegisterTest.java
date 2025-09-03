package me.rgunny.marketpulse.watchlist.application.in;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import me.rgunny.marketpulse.watchlist.domain.WatchTarget;
import me.rgunny.marketpulse.watchlist.domain.WatchTargetFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
record WatchTargetRegisterTest(WatchTargetRegister watchTargetRegister, EntityManager entityManager) {

    @Test
    @DisplayName("WatchTargetRegister register")
    void register() {
        WatchTarget watchTarget = watchTargetRegister.register(WatchTargetFixture.registerWatchTargetRequest());

        assertThat(watchTarget.getId()).isNotNull();
        assertThat(watchTarget.getStockName()).isEqualTo("NAVER");
    }

    @Test
    @DisplayName("WatchTargetRegister activate")
    void activate() {
        WatchTarget watchTarget = watchTargetRegister.register(WatchTargetFixture.registerWatchTargetRequest());
        watchTargetRegister.deactivate(watchTarget.getId());

        watchTargetRegister.activate(watchTarget.getId());

        assertThat(watchTarget.isActive()).isTrue();
    }

    @Test
    @DisplayName("WatchTargetRegister deactivate")
    void deactivate() {
        WatchTarget watchTarget = watchTargetRegister.register(WatchTargetFixture.registerWatchTargetRequest());

        watchTargetRegister.deactivate(watchTarget.getId());

        assertThat(watchTarget.isActive()).isFalse();
    }




}