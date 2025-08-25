package me.rgunny.marketpulse.event.marketdata.infrastructure.adapter.in.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.marketpulse.event.marketdata.infrastructure.adapter.in.init.AlertHistoryCleaner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * 알림 관리 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/alert")
@RequiredArgsConstructor
@ConditionalOnBean(AlertHistoryCleaner.class)
public class AlertManagementController {
    
    private final AlertHistoryCleaner alertHistoryCleaner;
    
    /**
     * 알림 이력 수동 초기화
     */
    @DeleteMapping("/history")
    public Mono<ResponseEntity<Map<String, Object>>> clearAlertHistory() {
        log.info("Alert history cleanup requested via API");
        
        return alertHistoryCleaner.cleanupManually()
            .map(count -> {
                Map<String, Object> response = Map.of(
                    "status", "success",
                    "message", "Alert history cleared",
                    "deletedCount", count
                );
                return ResponseEntity.ok(response);
            })
            .doOnSuccess(response -> log.info("Alert history cleanup completed"))
            .onErrorResume(error -> {
                log.error("Alert history cleanup failed: {}", error.getMessage());
                Map<String, Object> errorResponse = Map.of(
                    "status", "error",
                    "message", "Failed to clear alert history",
                    "error", error.getMessage()
                );
                return Mono.just(ResponseEntity.internalServerError().body(errorResponse));
            });
    }
}