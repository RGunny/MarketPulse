package me.rgunny.marketpulse.watchlist.domain;

import me.rgunny.marketpulse.watchlist.domain.dto.WatchTargetRegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class WatchTargetTest {

    WatchTarget watchTarget;

    @BeforeEach
    void setUp() {
        watchTarget = WatchTarget.register(WatchTargetFixture.registerWatchTargetRequest());
    }

    @Test
    void registerWatchTarget() {
        assertThat(watchTarget.getCreatedAt()).isNotNull();
    }

    @Test
    void activate() {
        watchTarget.deactivate();
        watchTarget.activate();

        assertThat(watchTarget.isActive()).isTrue();
    }

    @Test
    void activateFail() {
        assertThatThrownBy(() -> {
            watchTarget.activate();
        }).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void deactivate() {
        watchTarget.deactivate();

        assertThat(watchTarget.isActive()).isFalse();
    }

    @Test
    void deactivateFail() {
        watchTarget.deactivate();

        assertThatThrownBy(() -> watchTarget.deactivate()).isInstanceOf(IllegalStateException.class);
    }

}