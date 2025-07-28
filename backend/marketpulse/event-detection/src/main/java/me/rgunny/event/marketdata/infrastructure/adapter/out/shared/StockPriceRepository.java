package me.rgunny.event.marketdata.infrastructure.adapter.out.shared;

import me.rgunny.event.marketdata.domain.model.StockPrice;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface StockPriceRepository extends ReactiveMongoRepository<StockPrice, String> {
    
    // 종목별 최신 시세 조회
    Mono<StockPrice> findFirstBySymbolOrderByTimestampDesc(String symbol);
    
    // 종목별 시간 범위 조회
    Flux<StockPrice> findBySymbolAndTimestampBetweenOrderByTimestampDesc(
            String symbol, 
            LocalDateTime startTime, 
            LocalDateTime endTime
    );
    
    // 특정 시간 이전 데이터 삭제 (TTL 대체용)
    Mono<Void> deleteByTimestampBefore(LocalDateTime timestamp);
}