package me.rgunny.marketpulse.watchlist.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.marketpulse.common.core.exception.BusinessException;
import me.rgunny.marketpulse.watchlist.application.in.WatchTargetFinder;
import me.rgunny.marketpulse.watchlist.application.out.WatchTargetRepository;
import me.rgunny.marketpulse.watchlist.domain.WatchTarget;
import me.rgunny.marketpulse.watchlist.domain.error.WatchTargetErrorCode;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Repository
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class WatchTargetQueryService implements WatchTargetFinder {

    private final WatchTargetRepository watchTargetRepository;

    @Override
    public WatchTarget find(Long watchTargetId) {
        return watchTargetRepository.findById(watchTargetId)
                .orElseThrow(() -> new BusinessException("ID : " + watchTargetId,  WatchTargetErrorCode.WATCHTARGET_FIND_001));
    }

}
