package me.rgunny.marketpulse.event.marketdata.infrastructure.adapter.in.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.marketpulse.event.marketdata.application.port.in.StockMasterSyncUseCase;
import me.rgunny.marketpulse.event.marketdata.domain.model.SyncMode;
import me.rgunny.marketpulse.event.marketdata.domain.model.SyncResult;
import me.rgunny.marketpulse.event.marketdata.domain.error.StockMasterErrorCode;
import me.rgunny.marketpulse.common.core.response.Result;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * 종목 마스터 관리 API
 * 
 * 관리자용 종목 마스터 동기화 및 모니터링 기능 제공
 * 수동 동기화, 상태 조회, 통계 정보 등
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/stock-master")
@RequiredArgsConstructor
public class StockMasterAdminController {
    
    private final StockMasterSyncUseCase stockMasterSyncUseCase;
    
    /**
     * 수동 전체 동기화 실행
     * 
     * @return 동기화 결과
     */
    @PostMapping("/sync/full")
    public Mono<Result<SyncResultDto>> executeFullSync() {
        log.info("관리자 요청: 종목 마스터 전체 동기화");
        
        return stockMasterSyncUseCase.syncStockMaster(SyncMode.FULL)
                .map(this::toDto)
                .map(Result::success)
                .doOnSuccess(result -> log.info("전체 동기화 완료: {}", result))
                .doOnError(error -> log.error("전체 동기화 실패", error))
                .onErrorReturn(Result.failure(StockMasterErrorCode.SYNC_FAILED));
    }
    
    /**
     * 수동 증분 동기화 실행
     * 
     * @return 동기화 결과
     */
    @PostMapping("/sync/incremental")
    public Mono<Result<SyncResultDto>> executeIncrementalSync() {
        log.info("관리자 요청: 종목 마스터 증분 동기화");
        
        return stockMasterSyncUseCase.syncStockMaster(SyncMode.INCREMENTAL)
                .map(this::toDto)
                .map(Result::success)
                .doOnSuccess(result -> log.info("증분 동기화 완료: {}", result))
                .doOnError(error -> log.error("증분 동기화 실패", error))
                .onErrorReturn(Result.failure(StockMasterErrorCode.SYNC_FAILED));
    }
    
    /**
     * 특정 시장 동기화 실행
     * 
     * @param market 시장 구분 (KOSPI, KOSDAQ)
     * @param mode 동기화 모드 (FULL, INCREMENTAL)
     * @return 동기화 결과
     */
    @PostMapping("/sync/market/{market}")
    public Mono<Result<SyncResultDto>> executMarketSync(
            @PathVariable String market,
            @RequestParam(defaultValue = "FULL") String mode) {
        
        log.info("관리자 요청: {} 시장 {} 동기화", market, mode);
        
        SyncMode syncMode = SyncMode.valueOf(mode.toUpperCase());
        
        return stockMasterSyncUseCase.syncStockMasterByMarket(market, syncMode)
                .map(this::toDto)
                .map(Result::success)
                .doOnSuccess(result -> log.info("{} 시장 동기화 완료: {}", market, result))
                .doOnError(error -> log.error("{} 시장 동기화 실패", market, error))
                .onErrorReturn(Result.failure(StockMasterErrorCode.SYNC_FAILED));
    }
    
    /**
     * 동기화 상태 조회
     * 
     * @return 동기화 진행 여부
     */
    @GetMapping("/sync/status")
    public Mono<Result<SyncStatusDto>> getSyncStatus() {
        return stockMasterSyncUseCase.isSyncing()
                .map(isSyncing -> new SyncStatusDto(
                        isSyncing,
                        isSyncing ? "동기화 진행 중" : "대기 중"
                ))
                .map(Result::success);
    }
    
    /**
     * 동기화 결과 DTO 변환
     */
    private SyncResultDto toDto(SyncResult result) {
        return new SyncResultDto(
                result.syncMode().name(),
                result.startTime().toString(),
                result.endTime().toString(),
                result.totalProcessed(),
                result.successCount(),
                result.failedCount(),
                result.newCount(),
                result.updatedCount(),
                result.getSuccessRate(),
                result.getDurationSeconds(),
                result.errors()
        );
    }
    
    /**
     * 동기화 결과 DTO
     */
    public record SyncResultDto(
            String syncMode,
            String startTime,
            String endTime,
            int totalProcessed,
            int successCount,
            int failedCount,
            int newCount,
            int updatedCount,
            double successRate,
            long durationSeconds,
            java.util.List<String> errors
    ) {}
    
    /**
     * 동기화 상태 DTO
     */
    public record SyncStatusDto(
            boolean isSyncing,
            String status
    ) {}
}