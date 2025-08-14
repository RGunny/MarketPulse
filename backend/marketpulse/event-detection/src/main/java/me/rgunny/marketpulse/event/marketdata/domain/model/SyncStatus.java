package me.rgunny.marketpulse.event.marketdata.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 종목 동기화 상태
 */
@Getter
@RequiredArgsConstructor
public enum SyncStatus {
    
    /**
     * 전체 성공
     * 모든 종목이 성공적으로 동기화됨
     */
    SUCCESS("성공", "모든 종목이 성공적으로 동기화되었습니다."),
    
    /**
     * 부분 성공
     * 일부 종목만 동기화 성공
     */
    PARTIAL("부분 성공", "일부 종목에서 오류가 발생했으나 동기화를 완료했습니다."),
    
    /**
     * 전체 실패
     * 동기화 완전 실패
     */
    FAILED("실패", "동기화에 실패했습니다.");
    
    private final String displayName;
    private final String description;
    
    /**
     * 성공 여부 확인
     */
    public boolean isSuccess() {
        return this == SUCCESS;
    }
    
    /**
     * 실패 여부 확인
     */
    public boolean isFailed() {
        return this == FAILED;
    }
    
    /**
     * 부분 성공 여부 확인
     */
    public boolean isPartial() {
        return this == PARTIAL;
    }
}