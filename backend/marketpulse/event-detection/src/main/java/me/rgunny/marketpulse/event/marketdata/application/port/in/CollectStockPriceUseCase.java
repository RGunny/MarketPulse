package me.rgunny.marketpulse.event.marketdata.application.port.in;

import me.rgunny.marketpulse.event.marketdata.domain.model.StockPrice;
import reactor.core.publisher.Mono;

public interface CollectStockPriceUseCase {
    
    /**
     * 종목 현재가 조회 (캐시 우선, 없으면 API 호출)
     * @param symbol 종목코드 (예: 005930)
     * @return 현재가 정보
     */
    Mono<StockPrice> getCurrentPrice(String symbol);
    
    /**
     * 종목 현재가 강제 갱신 (캐시 무시하고 API 호출)
     * @param symbol 종목코드 (예: 005930)
     * @return 최신 현재가 정보
     */
    Mono<StockPrice> refreshCurrentPrice(String symbol);
    
    /**
     * 종목 현재가 조회 및 MongoDB 저장
     * @param symbol 종목코드 (예: 005930)
     * @return 저장된 현재가 정보
     */
    Mono<StockPrice> getCurrentPriceAndSave(String symbol);
}