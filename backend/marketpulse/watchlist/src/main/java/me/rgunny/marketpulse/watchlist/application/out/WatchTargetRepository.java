package me.rgunny.marketpulse.watchlist.application.out;

import me.rgunny.marketpulse.watchlist.domain.WatchTarget;
import org.springframework.data.repository.Repository;

import java.util.Optional;

/**
 * WatchTarget 을 저장하거나 조회한다.
 */
public interface WatchTargetRepository extends Repository<WatchTarget, Long> {

    WatchTarget save(WatchTarget watchTarget);

    Optional<WatchTarget> findById(Long id);

    Optional<WatchTarget> findByStockCode(String stockCode);

}
