package me.rgunny.marketpulse.event.marketdata.application.port.in;

import me.rgunny.marketpulse.event.marketdata.domain.model.SyncMode;
import me.rgunny.marketpulse.event.marketdata.domain.model.SyncResult;
import reactor.core.publisher.Mono;

/**
 * 종목 마스터 데이터 동기화 유스케이스
 * KIS API로부터 종목 정보를 수집하여 자동으로 동기화
 */
public interface StockMasterSyncUseCase {
    
    /**
     * 종목 마스터 데이터 동기화 실행
     * 
     * @param syncMode 동기화 모드 (FULL: 전체, INCREMENTAL: 증분)
     * @return 동기화 결과
     */
    Mono<SyncResult> syncStockMaster(SyncMode syncMode);
    
    /**
     * 특정 시장의 종목만 동기화
     * 
     * @param market 시장 구분 (KOSPI, KOSDAQ)
     * @param syncMode 동기화 모드
     * @return 동기화 결과
     */
    Mono<SyncResult> syncStockMasterByMarket(String market, SyncMode syncMode);
    
    /**
     * 동기화 상태 조회
     * 
     * @return 현재 동기화 진행 여부
     */
    Mono<Boolean> isSyncing();
}