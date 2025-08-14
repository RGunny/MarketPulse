package me.rgunny.marketpulse.event.marketdata.infrastructure.adapter.in.rest;

import me.rgunny.marketpulse.event.marketdata.application.usecase.PriceAlertService;
import me.rgunny.marketpulse.event.marketdata.domain.model.StockPrice;
import me.rgunny.marketpulse.common.core.response.Result;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * 테스트용 Controller - gRPC 알림 직접 테스트
 */
@RestController
@RequestMapping("/api/v1/test")
public class GRpcTestController {
    
    private final PriceAlertService priceAlertService;
    
    public GRpcTestController(PriceAlertService priceAlertService) {
        this.priceAlertService = priceAlertService;
    }
    
    /**
     * 가짜 급등 알림 테스트
     */
    @PostMapping(value = "/price-alert/rise", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Result<String>> testRiseAlert() {
        // 가짜 급등 데이터 생성 (7% 상승)
        StockPrice stockPrice = createFakeStockPrice(new BigDecimal("7.0"));
        
        return priceAlertService.analyzeAndSendAlert(stockPrice)
                .then(Mono.just(Result.success("급등 알림 테스트 완료 - Slack 확인해보세요!")));
    }
    
    /**
     * 가짜 급락 알림 테스트
     */
    @PostMapping(value = "/price-alert/fall", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Result<String>> testFallAlert() {
        // 가짜 급락 데이터 생성 (-6% 하락)
        StockPrice stockPrice = createFakeStockPrice(new BigDecimal("-6.0"));
        
        return priceAlertService.analyzeAndSendAlert(stockPrice)
                .then(Mono.just(Result.success("급락 알림 테스트 완료 - Slack 확인해보세요!")));
    }
    
    /**
     * 가짜 상한가 알림 테스트
     */
    @PostMapping(value = "/price-alert/limit-up", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Result<String>> testLimitUpAlert() {
        // 가짜 상한가 데이터 생성 (30% 상승)
        StockPrice stockPrice = createFakeStockPrice(new BigDecimal("30.0"));
        
        return priceAlertService.analyzeAndSendAlert(stockPrice)
                .then(Mono.just(Result.success("상한가 알림 테스트 완료 - Slack 확인해보세요!")));
    }
    
    private StockPrice createFakeStockPrice(BigDecimal changeRate) {
        BigDecimal previousClose = new BigDecimal("70000");
        BigDecimal change = previousClose.multiply(changeRate).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal currentPrice = previousClose.add(change);
        
        return StockPrice.createWithTTL(
                "005930",
                "삼성전자",
                currentPrice,
                previousClose,
                currentPrice.add(new BigDecimal("500")), // 고가
                previousClose.subtract(new BigDecimal("500")), // 저가
                new BigDecimal("70200"), // 시가
                2000000L, // 거래량
                currentPrice.add(new BigDecimal("100")), // 매도호가1
                currentPrice.subtract(new BigDecimal("100")) // 매수호가1
        );
    }
}