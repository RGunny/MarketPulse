package me.rgunny.marketpulse.event.marketdata.application.port.out;

import me.rgunny.marketpulse.event.marketdata.domain.model.Stock;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * 종목 마스터 데이터 외부 연동 포트
 * KIS API와의 통신을 추상화
 */
public interface StockMasterPort {
    
    /**
     * KIS API로부터 전체 종목 마스터 조회
     * 
     * @return 종목 목록
     */
    Flux<Stock> fetchAllStocks();
    
    /**
     * 특정 시장의 종목 마스터 조회
     * 
     * @param market 시장 구분 (KOSPI, KOSDAQ)
     * @return 종목 목록
     */
    Flux<Stock> fetchStocksByMarket(String market);
    
    /**
     * 특정 종목 상세 정보 조회
     * 
     * @param symbol 종목 코드
     * @return 종목 정보
     */
    Mono<Stock> fetchStockDetail(String symbol);
    
    /**
     * 종목 마스터 업데이트 시각 조회
     * 
     * @return 마지막 업데이트 시각
     */
    Mono<String> getLastUpdateTime();
}