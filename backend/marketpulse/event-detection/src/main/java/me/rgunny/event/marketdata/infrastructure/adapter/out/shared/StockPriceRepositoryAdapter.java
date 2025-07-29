package me.rgunny.event.marketdata.infrastructure.adapter.out.shared;

import lombok.RequiredArgsConstructor;
import me.rgunny.event.marketdata.application.port.out.shared.MarketDataRepositoryPort;
import me.rgunny.event.marketdata.domain.model.StockPrice;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class StockPriceRepositoryAdapter implements MarketDataRepositoryPort {

    private final StockPriceRepository stockPriceRepository;

    @Override
    public Mono<StockPrice> save(StockPrice stockPrice) {
        return stockPriceRepository.save(stockPrice);
    }

    @Override
    public Mono<StockPrice> findLatestBySymbol(String symbol) {
        return stockPriceRepository.findFirstBySymbolOrderByTimestampDesc(symbol);
    }

    @Override
    public Flux<StockPrice> findBySymbolAndTimestampBetween(String symbol, LocalDateTime startTime, LocalDateTime endTime) {
        return stockPriceRepository.findBySymbolAndTimestampBetweenOrderByTimestampDesc(symbol, startTime, endTime);
    }

    @Override
    public Mono<Long> deleteOldData(LocalDateTime cutoffTime) {
        return stockPriceRepository.deleteByTimestampBefore(cutoffTime)
                .thenReturn(0L); // MongoDB의 deleteBy는 void를 반환하므로 0L로 대신함
    }
}