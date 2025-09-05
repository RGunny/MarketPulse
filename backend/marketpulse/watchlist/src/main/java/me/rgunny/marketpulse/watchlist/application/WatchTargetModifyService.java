package me.rgunny.marketpulse.watchlist.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.marketpulse.common.core.exception.BusinessException;
import me.rgunny.marketpulse.watchlist.application.in.WatchTargetFinder;
import me.rgunny.marketpulse.watchlist.application.in.WatchTargetRegister;
import me.rgunny.marketpulse.watchlist.application.out.EventPublisher;
import me.rgunny.marketpulse.watchlist.application.out.WatchTargetRepository;
import me.rgunny.marketpulse.watchlist.domain.WatchTarget;
import me.rgunny.marketpulse.watchlist.domain.dto.WatchTargetRegisterRequest;
import me.rgunny.marketpulse.watchlist.domain.error.WatchTargetErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class WatchTargetModifyService implements WatchTargetRegister {

    private final WatchTargetFinder watchTargetFinder;
    private final WatchTargetRepository watchTargetRepository;
    private final EventPublisher publisher;

    @Override
    public WatchTarget register(WatchTargetRegisterRequest registerRequest) {
        checkDuplicateStockCode(registerRequest.stockCode());

        WatchTarget watchTarget = WatchTarget.register(registerRequest);
        WatchTarget savedWatchTarget = watchTargetRepository.save(watchTarget);

        publisher.publishCreated(savedWatchTarget);

        return watchTarget;
    }

    @Override
    public WatchTarget activate(Long watchTargetId) {
        WatchTarget watchTarget = watchTargetFinder.find(watchTargetId);
        watchTarget.activate();
        WatchTarget savedWatchTarget = watchTargetRepository.save(watchTarget);

        publisher.publishCreated(savedWatchTarget);

        return savedWatchTarget;
    }

    @Override
    public WatchTarget deactivate(Long watchTargetId) {
        WatchTarget watchTarget = watchTargetFinder.find(watchTargetId);
        watchTarget.deactivate();
        WatchTarget savedWatchTarget = watchTargetRepository.save(watchTarget);

        publisher.publishCreated(savedWatchTarget);

        return savedWatchTarget;
    }

    private void checkDuplicateStockCode(String stockCode) {
        if (watchTargetRepository.findByStockCode(stockCode).isPresent()) {
            throw new BusinessException("StockCode: " + stockCode, WatchTargetErrorCode.WATCHTARGET_STOCK_001);
        }
    }

}
