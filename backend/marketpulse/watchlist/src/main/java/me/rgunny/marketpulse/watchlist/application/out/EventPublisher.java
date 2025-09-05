package me.rgunny.marketpulse.watchlist.application.out;

import me.rgunny.marketpulse.watchlist.domain.WatchTarget;

public interface EventPublisher {

    void publishCreated(WatchTarget watchTarget);

    void publishActivated(WatchTarget watchTarget);

    void publishDeactivated(WatchTarget watchTarget);
}
