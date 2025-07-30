package me.rgunny.event.marketdata.application.usecase;

import me.rgunny.event.marketdata.application.port.in.GetStockPriceUseCase;
import me.rgunny.event.marketdata.application.port.in.CollectStockPriceUseCase;
import me.rgunny.event.marketdata.application.port.out.ExternalApiPort;
import me.rgunny.event.marketdata.application.port.out.shared.MarketDataCachePort;
import me.rgunny.event.marketdata.application.port.out.shared.MarketDataRepositoryPort;
import me.rgunny.event.marketdata.domain.model.StockPrice;
import me.rgunny.event.shared.domain.value.MarketDataType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class GetStockPriceService implements GetStockPriceUseCase, CollectStockPriceUseCase {
    
    private static final Duration CACHE_TTL = Duration.ofMinutes(1);
    
    private final ExternalApiPort externalApiPort;
    private final MarketDataCachePort marketDataCachePort;
    private final MarketDataRepositoryPort marketDataRepositoryPort;
    private final PriceAlertService priceAlertService;
    
    public GetStockPriceService(ExternalApiPort externalApiPort,
                               MarketDataCachePort marketDataCachePort,
                               MarketDataRepositoryPort marketDataRepositoryPort,
                               PriceAlertService priceAlertService) {
        this.externalApiPort = externalApiPort;
        this.marketDataCachePort = marketDataCachePort;
        this.marketDataRepositoryPort = marketDataRepositoryPort;
        this.priceAlertService = priceAlertService;
    }
    
    @Override
    public Mono<StockPrice> getCurrentPrice(String symbol) {
        return marketDataCachePort.getStockPrice(symbol)
                .switchIfEmpty(getFromApiAndCache(symbol));
    }
    
    @Override
    public Mono<StockPrice> refreshCurrentPrice(String symbol) {
        return marketDataCachePort.deleteStockPrice(symbol)
                .then(getFromApiAndCache(symbol));
    }
    
    @Override
    public Mono<StockPrice> getCurrentPriceAndSave(String symbol) {
        return getCurrentPrice(symbol)
                .flatMap(stockPrice -> 
                    marketDataRepositoryPort.save(stockPrice)
                            .flatMap(savedStockPrice -> 
                                priceAlertService.analyzeAndSendAlert(savedStockPrice)
                                        .thenReturn(savedStockPrice)
                            )
                );
    }
    
    
    private Mono<StockPrice> getFromApiAndCache(String symbol) {
        return externalApiPort.fetchMarketData(symbol, MarketDataType.STOCK, StockPrice.class)
                .flatMap(stockPrice -> 
                    marketDataCachePort.saveStockPrice(stockPrice, CACHE_TTL)
                            .thenReturn(stockPrice)
                );
    }
}