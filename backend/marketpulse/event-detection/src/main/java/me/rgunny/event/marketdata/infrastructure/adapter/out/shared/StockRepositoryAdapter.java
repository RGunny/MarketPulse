package me.rgunny.event.marketdata.infrastructure.adapter.out.shared;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.event.marketdata.application.port.out.shared.StockPort;
import me.rgunny.event.marketdata.domain.model.MarketType;
import me.rgunny.event.marketdata.domain.model.Stock;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockRepositoryAdapter implements StockPort {
    
    private final StockRepository stockRepository;
    
    @Override
    public Mono<Stock> save(Stock stock) {
        return stockRepository.save(stock)
            .doOnSuccess(saved -> log.debug("Stock saved: symbol={}, name={}", 
                saved.getSymbol(), saved.getName()))
            .doOnError(error -> log.error("Failed to save Stock: symbol={}", 
                stock.getSymbol(), error));
    }
    
    @Override
    // TODO: Redis 캐시 설정 후 @Cacheable 추가 필요
    // @Cacheable(value = "stock", key = "#symbol", cacheManager = "redisCacheManager")
    public Mono<Stock> findBySymbol(String symbol) {
        return stockRepository.findBySymbol(symbol)
            .doOnSuccess(stock -> {
                if (stock != null) {
                    log.debug("Stock found: symbol={}, name={}", symbol, stock.getName());
                } else {
                    log.debug("Stock not found: symbol={}", symbol);
                }
            });
    }
    
    @Override
    public Flux<Stock> findByNameContaining(String name) {
        return stockRepository.findByNameContaining(name)
            .doOnComplete(() -> log.debug("Search completed for name containing: {}", name));
    }
    
    @Override
    public Flux<Stock> findActiveStocksByMarket(MarketType marketType) {
        return stockRepository.findByMarketTypeAndIsActive(marketType, true)
            .doOnComplete(() -> log.debug("Active stocks loaded for market: {}", marketType));
    }
    
    @Override
    public Flux<Stock> findAllActiveStocks() {
        return stockRepository.findByIsActiveTrue()
            .doOnComplete(() -> log.debug("All active stocks loaded"));
    }
    
    @Override
    public Flux<Stock> findBySector(String sectorCode) {
        return stockRepository.findBySectorCode(sectorCode)
            .doOnComplete(() -> log.debug("Stocks loaded for sector: {}", sectorCode));
    }
    
    @Override
    public Flux<Stock> findAllETFs() {
        return stockRepository.findByIsETFTrue()
            .doOnComplete(() -> log.debug("All ETFs loaded"));
    }
    
    @Override
    public Mono<Boolean> existsBySymbol(String symbol) {
        return stockRepository.existsBySymbol(symbol);
    }
    
    @Override
    public Flux<Stock> findBySymbols(Flux<String> symbols) {
        return stockRepository.findBySymbolIn(symbols)
            .doOnComplete(() -> log.debug("Multiple stocks loaded by symbols"));
    }
    
    @Override
    public Mono<Stock> update(Stock stock) {
        return stockRepository.save(stock)
            .doOnSuccess(updated -> log.info("StockMaster updated: symbol={}, name={}", 
                updated.getSymbol(), updated.getName()))
            .doOnError(error -> log.error("Failed to update StockMaster: symbol={}", 
                stock.getSymbol(), error));
    }
    
    @Override
    public Mono<Void> deleteBySymbol(String symbol) {
        return stockRepository.findBySymbol(symbol)
            .flatMap(stockRepository::delete)
            .doOnSuccess(unused -> log.warn("StockMaster deleted: symbol={}", symbol))
            .doOnError(error -> log.error("Failed to delete StockMaster: symbol={}", symbol, error));
    }
    
    @Override
    public Mono<Long> count() {
        return stockRepository.count();
    }
    
    @Override
    public Mono<Long> countActiveStocks() {
        return stockRepository.findByIsActiveTrue()
            .count();
    }
}