package me.rgunny.marketpulse.event.marketdata.application.port.out.shared;

import me.rgunny.marketpulse.event.marketdata.domain.model.StockPrice;
import reactor.core.publisher.Mono;

import java.time.Duration;

public interface MarketDataCachePort {
    
    /**
     * 캐시에서 현재가 조회
     * @param symbol 종목코드
     * @return 캐시된 현재가 정보 (없으면 empty)
     */
    Mono<StockPrice> getStockPrice(String symbol);
    
    /**
     * 현재가 캐시 저장
     * @param stockPrice 저장할 현재가 정보
     * @param ttl 캐시 유지 시간
     * @return 저장 완료 신호
     */
    Mono<Void> saveStockPrice(StockPrice stockPrice, Duration ttl);
    
    /**
     * 특정 종목 캐시 삭제
     * @param symbol 종목코드
     * @return 삭제 완료 신호
     */
    Mono<Void> deleteStockPrice(String symbol);
    
    /**
     * 캐시 TTL 조회
     * @param symbol 종목코드
     * @return 남은 TTL (초)
     */
    Mono<Long> getStockPriceTtl(String symbol);
}