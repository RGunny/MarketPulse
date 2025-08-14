package me.rgunny.marketpulse.event.marketdata.application.port.out.shared;

import me.rgunny.marketpulse.event.marketdata.domain.model.MarketType;
import me.rgunny.marketpulse.event.marketdata.domain.model.Stock;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 종목 정보 관리 Port
 */
public interface StockPort {
    
    /**
     * 종목 마스터 정보 저장 (upsert: 없으면 insert, 있으면 update)
     */
    Mono<Stock> save(Stock stock);
    
    /**
     * 종목코드로 조회
     */
    Mono<Stock> findBySymbol(String symbol);
    
    /**
     * 종목명으로 조회 (부분 일치)
     */
    Flux<Stock> findByNameContaining(String name);
    
    /**
     * 시장별 활성 종목 조회
     */
    Flux<Stock> findActiveStocksByMarket(MarketType marketType);
    
    /**
     * 전체 활성 종목 조회
     */
    Flux<Stock> findAllActiveStocks();
    
    /**
     * 업종별 종목 조회
     */
    Flux<Stock> findBySector(String sectorCode);
    
    /**
     * ETF 목록 조회
     */
    Flux<Stock> findAllETFs();
    
    /**
     * 종목 존재 여부 확인
     */
    Mono<Boolean> existsBySymbol(String symbol);
    
    /**
     * 여러 종목 한번에 조회
     */
    Flux<Stock> findBySymbols(Flux<String> symbols);
    
    
    /**
     * 종목 삭제 (실제 삭제보다는 비활성화 권장)
     */
    Mono<Void> deleteBySymbol(String symbol);
    
    /**
     * 전체 종목 수 조회
     */
    Mono<Long> count();
    
    /**
     * 활성 종목 수 조회
     */
    Mono<Long> countActiveStocks();
    
    /**
     * 전체 종목 삭제 (전체 동기화 시 사용)
     */
    Mono<Void> deleteAll();
}