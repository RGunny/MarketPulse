package me.rgunny.marketpulse.watchlist.config;

import me.rgunny.marketpulse.watchlist.application.out.EventPublisher;
import me.rgunny.marketpulse.watchlist.domain.WatchTarget;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class TestEventPublisher implements EventPublisher {

    @Override
    public void publishCreated(WatchTarget watchTarget) {

    }

    @Override
    public void publishActivated(WatchTarget watchTarget) {

    }

    @Override
    public void publishDeactivated(WatchTarget watchTarget) {

    }
}
