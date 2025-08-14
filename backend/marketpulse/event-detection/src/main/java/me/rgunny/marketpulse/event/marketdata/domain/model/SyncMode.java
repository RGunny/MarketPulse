package me.rgunny.marketpulse.event.marketdata.domain.model;

/**
 * 종목 동기화 모드
 */
public enum SyncMode {
    
    /**
     * 전체 동기화
     * - 기존 데이터 삭제 후 전체 재구축
     * - 매일 새벽 실행
     */
    FULL,
    
    /**
     * 증분 동기화
     * - 신규/변경된 종목만 업데이트
     * - 매시간 실행
     */
    INCREMENTAL
}