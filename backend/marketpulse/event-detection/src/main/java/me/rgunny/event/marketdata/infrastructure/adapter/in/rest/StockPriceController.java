package me.rgunny.event.marketdata.infrastructure.adapter.in.rest;

import me.rgunny.marketpulse.common.response.Result;
import me.rgunny.event.marketdata.domain.error.StockPriceErrorCode;
import me.rgunny.event.marketdata.application.port.in.GetStockPriceUseCase;
import me.rgunny.event.marketdata.domain.model.StockPrice;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/stocks")
public class StockPriceController {
    
    private final GetStockPriceUseCase getStockPriceUseCase;
    
    public StockPriceController(GetStockPriceUseCase getStockPriceUseCase) {
        this.getStockPriceUseCase = getStockPriceUseCase;
    }
    
    /**
     * 종목 현재가 조회 (캐시 우선)
     * @param symbol 종목코드 (예: 005930)
     * @return 현재가 정보
     */
    @GetMapping(value = "/{symbol}/price", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Result<StockPrice>> getCurrentPrice(@PathVariable String symbol) {
        return getStockPriceUseCase.getCurrentPrice(symbol)
                .map(Result::success)
                .onErrorResume(error -> Mono.just(Result.failure(StockPriceErrorCode.STOCK_PRICE_ERROR)));
    }
    
    /**
     * 종목 현재가 강제 갱신 (캐시 무시)
     * @param symbol 종목코드 (예: 005930)
     * @return 최신 현재가 정보
     */
    @PostMapping(value = "/{symbol}/price/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Result<StockPrice>> refreshCurrentPrice(@PathVariable String symbol) {
        return getStockPriceUseCase.refreshCurrentPrice(symbol)
                .map(Result::success)
                .onErrorResume(error -> Mono.just(Result.failure(StockPriceErrorCode.STOCK_PRICE_REFRESH_ERROR)));
    }
    
    /**
     * 종목 현재가 조회 및 저장 (MongoDB에 이력 저장)
     * @param symbol 종목코드 (예: 005930)
     * @return 저장된 현재가 정보
     */
    @PostMapping(value = "/{symbol}/price/save", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<Result<StockPrice>> getCurrentPriceAndSave(@PathVariable String symbol) {
        return getStockPriceUseCase.getCurrentPriceAndSave(symbol)
                .map(Result::success)
                .onErrorResume(error -> Mono.just(Result.failure(StockPriceErrorCode.STOCK_PRICE_SAVE_ERROR)));
    }
}