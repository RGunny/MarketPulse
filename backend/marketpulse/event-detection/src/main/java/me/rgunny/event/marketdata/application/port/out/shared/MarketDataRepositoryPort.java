package me.rgunny.event.marketdata.application.port.out.shared;

import me.rgunny.event.marketdata.domain.model.StockPrice;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface MarketDataRepositoryPort {
    
    /**
     * 주식 시세 데이터 저장
     * @param stockPrice 저장할 시세 정보
     * @return 저장된 시세 정보
     */
    Mono<StockPrice> save(StockPrice stockPrice);
    
    /**
     * 종목별 최신 시세 조회
     * @param symbol 종목코드
     * @return 최신 시세 정보
     */
    Mono<StockPrice> findLatestBySymbol(String symbol);
    
    /**
     * 종목별 시간 범위 내 시세 이력 조회
     * @param symbol 종목코드
     * @param startTime 시작 시간
     * @param endTime 종료 시간
     * @return 시세 이력 리스트
     */
    Flux<StockPrice> findBySymbolAndTimestampBetween(String symbol, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 오래된 데이터 정리 (TTL 대체)
     * @param cutoffTime 삭제 기준 시간
     * @return 삭제된 데이터 수
     */
    Mono<Long> deleteOldData(LocalDateTime cutoffTime);
}