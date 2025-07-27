package me.rgunny.event.application.port.input;

import reactor.core.publisher.Mono;

/**
 * 주식 시세 수집 유스케이스
 * 실시간 배치 수집 및 우선순위별 스케줄링 관리
 */
public interface StockCollectionUseCase {
    
    /**
     * 활성화된 모든 감시 대상 주식의 시세를 수집
     * 각 종목의 수집 주기(collectInterval)를 고려하여 처리
     * 
     * @return 수집 완료 신호
     */
    Mono<Void> collectActiveStockPrices();
    
    /**
     * 높은 우선순위 종목 시세를 즉시 수집
     * 우선순위 1-3 범위의 종목만 대상
     * 
     * @return 수집 완료 신호
     */
    Mono<Void> collectHighPriorityStocks();
    
    /**
     * 특정 카테고리 종목 시세를 수집
     * 
     * @param category 수집할 카테고리
     * @return 수집 완료 신호
     */
    Mono<Void> collectStocksByCategory(String category);
}