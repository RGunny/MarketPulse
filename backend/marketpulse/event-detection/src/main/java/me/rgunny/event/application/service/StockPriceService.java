package me.rgunny.event.application.service;

import me.rgunny.event.application.port.input.GetStockPriceUseCase;
import me.rgunny.event.application.port.output.KISApiPort;
import me.rgunny.event.application.port.output.StockPriceCachePort;
import me.rgunny.event.domain.stock.StockPrice;
import me.rgunny.event.infrastructure.repository.StockPriceRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class StockPriceService implements GetStockPriceUseCase {
    
    private static final Duration CACHE_TTL = Duration.ofMinutes(1); // 1분 캐시
    
    private final KISApiPort kisApiPort;
    private final StockPriceCachePort stockPriceCachePort;
    private final StockPriceRepository stockPriceRepository;
    
    public StockPriceService(KISApiPort kisApiPort,
                            StockPriceCachePort stockPriceCachePort,
                            StockPriceRepository stockPriceRepository) {
        this.kisApiPort = kisApiPort;
        this.stockPriceCachePort = stockPriceCachePort;
        this.stockPriceRepository = stockPriceRepository;
    }
    
    @Override
    public Mono<StockPrice> getCurrentPrice(String symbol) {
        return stockPriceCachePort.getStockPrice(symbol)
                .switchIfEmpty(getFromApiAndCache(symbol));
    }
    
    @Override
    public Mono<StockPrice> refreshCurrentPrice(String symbol) {
        return stockPriceCachePort.deleteStockPrice(symbol)
                .then(getFromApiAndCache(symbol));
    }
    
    @Override
    public Mono<StockPrice> getCurrentPriceAndSave(String symbol) {
        return getCurrentPrice(symbol)
                .flatMap(stockPriceRepository::save);
    }
    
    private Mono<StockPrice> getFromApiAndCache(String symbol) {
        return kisApiPort.getCurrentPrice(symbol)
                .flatMap(stockPrice -> 
                    stockPriceCachePort.saveStockPrice(stockPrice, CACHE_TTL)
                            .thenReturn(stockPrice)
                );
    }
}