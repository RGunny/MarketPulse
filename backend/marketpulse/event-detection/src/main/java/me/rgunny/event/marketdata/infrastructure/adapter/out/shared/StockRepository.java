package me.rgunny.event.marketdata.infrastructure.adapter.out.shared;

import me.rgunny.event.marketdata.domain.model.MarketType;
import me.rgunny.event.marketdata.domain.model.Stock;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 종목 MongoDB Repository
 */
@Repository
public interface StockRepository extends ReactiveMongoRepository<Stock, String> {
    
    /**
     * 종목코드로 조회
     */
    Mono<Stock> findBySymbol(String symbol);
    
    /**
     * 종목명으로 조회 (부분 일치)
     */
    Flux<Stock> findByNameContaining(String name);
    
    /**
     * 시장 구분으로 조회
     */
    Flux<Stock> findByMarketType(MarketType marketType);
    
    /**
     * 업종별 종목 조회
     */
    Flux<Stock> findBySectorCode(String sectorCode);
    
    /**
     * 활성 종목만 조회
     */
    Flux<Stock> findByIsActiveTrue();
    
    /**
     * 시장 구분 및 활성 여부로 조회
     */
    Flux<Stock> findByMarketTypeAndIsActive(MarketType marketType, boolean isActive);
    
    /**
     * ETF 목록 조회
     */
    Flux<Stock> findByIsETFTrue();
    
    /**
     * 종목코드 존재 여부 확인
     */
    Mono<Boolean> existsBySymbol(String symbol);
    
    /**
     * 여러 종목코드로 한번에 조회
     */
    Flux<Stock> findBySymbolIn(Flux<String> symbols);
}