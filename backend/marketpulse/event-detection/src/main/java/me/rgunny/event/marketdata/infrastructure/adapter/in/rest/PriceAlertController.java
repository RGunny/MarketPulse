package me.rgunny.event.marketdata.infrastructure.adapter.in.rest;

import me.rgunny.event.marketdata.application.usecase.PriceAlertService;
import me.rgunny.event.marketdata.domain.error.StockPriceErrorCode;
import me.rgunny.marketpulse.common.core.response.Result;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * 가격 알림 관련 REST Controller
 */
@RestController
@RequestMapping("/api/v1/price-alerts")
public class PriceAlertController {
    
    private final PriceAlertService priceAlertService;
    
    public PriceAlertController(PriceAlertService priceAlertService) {
        this.priceAlertService = priceAlertService;
    }
    
    /**
     * 알림 서비스 상태 확인
     * @return 알림 서비스 상태
     */
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Result<String>> checkNotificationHealth() {
        return priceAlertService.checkNotificationServiceHealth()
                .map(Result::success)
                .onErrorResume(error -> Mono.just(Result.failure(StockPriceErrorCode.STOCK_PRICE_004)));
    }
}