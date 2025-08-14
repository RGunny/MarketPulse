package me.rgunny.marketpulse.event.unit.infrastructure.adapter.input.rest;

import me.rgunny.marketpulse.event.marketdata.domain.error.StockPriceErrorCode;
import me.rgunny.marketpulse.event.marketdata.application.port.in.GetStockPriceUseCase;
import me.rgunny.marketpulse.event.marketdata.domain.model.StockPrice;
import me.rgunny.marketpulse.event.marketdata.infrastructure.adapter.in.rest.StockPriceController;
import me.rgunny.marketpulse.common.core.response.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockPriceController 단위 테스트")
class StockPriceControllerTest {
    
    @Mock
    private GetStockPriceUseCase getStockPriceUseCase;
    
    private StockPriceController controller;
    
    private static final String SYMBOL = "005930";
    
    @BeforeEach
    void setUp() {
        controller = new StockPriceController(getStockPriceUseCase);
    }
    
    @Test
    @DisplayName("현재가 조회 성공")
    void givenValidSymbol_whenGetCurrentPrice_thenReturnsSuccessResult() {
        // given
        StockPrice stockPrice = createSampleStockPrice();
        given(getStockPriceUseCase.getCurrentPrice(SYMBOL)).willReturn(Mono.just(stockPrice));
        
        // when
        Mono<Result<StockPrice>> result = controller.getCurrentPrice(SYMBOL);
        
        // then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.isSuccess()).isTrue();
                    StockPrice data = response.dataOrThrow();
                    assertThat(data.getSymbol()).isEqualTo(SYMBOL);
                    assertThat(data.getCurrentPrice()).isEqualByComparingTo(new BigDecimal("71000"));
                })
                .verifyComplete();
        
        verify(getStockPriceUseCase).getCurrentPrice(SYMBOL);
    }
    
    @Test
    @DisplayName("현재가 조회 실패 시 에러 응답")
    void givenServiceError_whenGetCurrentPrice_thenReturnsErrorResult() {
        // given
        RuntimeException serviceError = new RuntimeException("API 호출 실패");
        given(getStockPriceUseCase.getCurrentPrice(SYMBOL)).willReturn(Mono.error(serviceError));
        
        // when
        Mono<Result<StockPrice>> result = controller.getCurrentPrice(SYMBOL);
        
        // then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.isFailure()).isTrue();
                    assertThat(response.code()).isEqualTo(StockPriceErrorCode.STOCK_PRICE_001.code());
                    assertThat(response.message()).isEqualTo(StockPriceErrorCode.STOCK_PRICE_001.message());
                })
                .verifyComplete();
    }
    
    @Test
    @DisplayName("현재가 강제 갱신 성공")
    void givenValidSymbol_whenRefreshCurrentPrice_thenReturnsSuccessResult() {
        // given
        StockPrice stockPrice = createSampleStockPrice();
        given(getStockPriceUseCase.refreshCurrentPrice(SYMBOL)).willReturn(Mono.just(stockPrice));
        
        // when
        Mono<Result<StockPrice>> result = controller.refreshCurrentPrice(SYMBOL);
        
        // then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.isSuccess()).isTrue();
                    StockPrice data = response.dataOrThrow();
                    assertThat(data.getSymbol()).isEqualTo(SYMBOL);
                    assertThat(data.getCurrentPrice()).isEqualByComparingTo(new BigDecimal("71000"));
                })
                .verifyComplete();
        
        verify(getStockPriceUseCase).refreshCurrentPrice(SYMBOL);
    }
    
    @Test
    @DisplayName("현재가 강제 갱신 실패 시 에러 응답")
    void givenServiceError_whenRefreshCurrentPrice_thenReturnsErrorResult() {
        // given
        RuntimeException serviceError = new RuntimeException("갱신 실패");
        given(getStockPriceUseCase.refreshCurrentPrice(SYMBOL)).willReturn(Mono.error(serviceError));
        
        // when
        Mono<Result<StockPrice>> result = controller.refreshCurrentPrice(SYMBOL);
        
        // then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.isFailure()).isTrue();
                    assertThat(response.code()).isEqualTo(StockPriceErrorCode.STOCK_PRICE_002.code());
                    assertThat(response.message()).isEqualTo(StockPriceErrorCode.STOCK_PRICE_002.message());
                })
                .verifyComplete();
    }
    
    @Test
    @DisplayName("현재가 조회 및 저장 성공")
    void givenValidSymbol_whenGetCurrentPriceAndSave_thenReturnsSuccessResult() {
        // given
        StockPrice stockPrice = createSampleStockPrice();
        given(getStockPriceUseCase.getCurrentPriceAndSave(SYMBOL)).willReturn(Mono.just(stockPrice));
        
        // when
        Mono<Result<StockPrice>> result = controller.getCurrentPriceAndSave(SYMBOL);
        
        // then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.isSuccess()).isTrue();
                    StockPrice data = response.dataOrThrow();
                    assertThat(data.getSymbol()).isEqualTo(SYMBOL);
                    assertThat(data.getCurrentPrice()).isEqualByComparingTo(new BigDecimal("71000"));
                })
                .verifyComplete();
        
        verify(getStockPriceUseCase).getCurrentPriceAndSave(SYMBOL);
    }
    
    @Test
    @DisplayName("현재가 조회 및 저장 실패 시 에러 응답")
    void givenServiceError_whenGetCurrentPriceAndSave_thenReturnsErrorResult() {
        // given
        RuntimeException serviceError = new RuntimeException("저장 실패");
        given(getStockPriceUseCase.getCurrentPriceAndSave(SYMBOL)).willReturn(Mono.error(serviceError));
        
        // when
        Mono<Result<StockPrice>> result = controller.getCurrentPriceAndSave(SYMBOL);
        
        // then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.isFailure()).isTrue();
                    assertThat(response.code()).isEqualTo(StockPriceErrorCode.STOCK_PRICE_003.code());
                    assertThat(response.message()).isEqualTo(StockPriceErrorCode.STOCK_PRICE_003.message());
                })
                .verifyComplete();
    }
    
    private StockPrice createSampleStockPrice() {
        return StockPrice.createWithTTL(
                SYMBOL,
                "삼성전자",
                new BigDecimal("71000"),    // 현재가
                new BigDecimal("70000"),    // 전일종가
                new BigDecimal("71500"),    // 고가
                new BigDecimal("70500"),    // 저가
                new BigDecimal("70800"),    // 시가
                1000000L,                   // 거래량
                new BigDecimal("71100"),    // 매도호가1
                new BigDecimal("70900")     // 매수호가1
        );
    }
}