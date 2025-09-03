package me.rgunny.marketpulse.watchlist.application.in;

import me.rgunny.marketpulse.watchlist.domain.WatchTarget;

/**
 * WatchTarget 을 조회한다.
 */
public interface WatchTargetFinder {

    WatchTarget find(Long id);

}
