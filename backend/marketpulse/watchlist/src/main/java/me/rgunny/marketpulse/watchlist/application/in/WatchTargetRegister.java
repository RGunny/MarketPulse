package me.rgunny.marketpulse.watchlist.application.in;

import me.rgunny.marketpulse.watchlist.domain.WatchTarget;
import me.rgunny.marketpulse.watchlist.domain.dto.WatchTargetRegisterRequest;

/**
 * WatchTarget 등록과 관련된 기능을 제공한다.
 */
public interface WatchTargetRegister {

    WatchTarget register(WatchTargetRegisterRequest registerRequest);

    WatchTarget activate(Long watchTargetId);

    WatchTarget deactivate(Long watchTargetId);

}
