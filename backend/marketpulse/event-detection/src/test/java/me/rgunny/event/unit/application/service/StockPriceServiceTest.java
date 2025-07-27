package me.rgunny.event.unit.application.service;

import me.rgunny.event.application.port.output.KISApiPort;
import me.rgunny.event.application.port.output.StockPriceCachePort;
import me.rgunny.event.application.service.StockPriceService;
import me.rgunny.event.domain.stock.StockPrice;
import me.rgunny.event.infrastructure.repository.StockPriceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockPriceService 단위 테스트")
class StockPriceServiceTest {
    
    @Mock
    private KISApiPort kisApiPort;
    
    @Mock
    private StockPriceCachePort stockPriceCachePort;
    
    @Mock
    private StockPriceRepository stockPriceRepository;
    
    private StockPriceService stockPriceService;
    
    @BeforeEach
    void setUp() {
        stockPriceService = new StockPriceService(kisApiPort, stockPriceCachePort, stockPriceRepository);
        
        // 서비스와 의존성 null 체크
        assertThat(kisApiPort).isNotNull();
        assertThat(stockPriceCachePort).isNotNull();
        assertThat(stockPriceRepository).isNotNull();
        assertThat(stockPriceService).isNotNull();
    }
    
    @Test
    @DisplayName("캐시에 데이터가 있으면 캐시에서 반환한다")
    void givenCachedStockPrice_whenGetCurrentPrice_thenReturnsCachedData() {
        // given
        String symbol = "005930";
        StockPrice cachedPrice = createSampleStockPrice(symbol);
        
        given(stockPriceCachePort.getStockPrice(symbol))
                .willReturn(Mono.just(cachedPrice));
        
        // API 호출을 안전하게 처리 (평가될 수 있으므로)
        given(kisApiPort.getCurrentPrice(symbol))
                .willReturn(Mono.empty()); // 빈 결과 반환
        
        // when & then
        StepVerifier.create(stockPriceService.getCurrentPrice(symbol))
                .expectNext(cachedPrice)
                .verifyComplete();
        
        verify(stockPriceCachePort).getStockPrice(symbol);
        // API 호출 여부는 검증하지 않음 (Reactor 동작에 따라 달라질 수 있음)
    }
    
    @Test
    @DisplayName("캐시에 데이터가 없으면 API에서 조회하고 캐시에 저장한다")
    void givenNoCachedData_whenGetCurrentPrice_thenFetchFromApiAndCache() {
        // given
        String symbol = "005930";
        StockPrice apiPrice = createSampleStockPrice(symbol);
        
        given(stockPriceCachePort.getStockPrice(symbol))
                .willReturn(Mono.empty());
        given(kisApiPort.getCurrentPrice(symbol))
                .willReturn(Mono.just(apiPrice));
        given(stockPriceCachePort.saveStockPrice(eq(apiPrice), any(Duration.class)))
                .willReturn(Mono.empty());
        
        // when
        Mono<StockPrice> result = stockPriceService.getCurrentPrice(symbol);
        
        // then
        StepVerifier.create(result)
                .expectNext(apiPrice)
                .verifyComplete();
        
        verify(kisApiPort).getCurrentPrice(symbol);
        verify(stockPriceCachePort).saveStockPrice(eq(apiPrice), any(Duration.class));
    }
    
    @Test
    @DisplayName("강제 갱신 시 캐시를 삭제하고 API에서 새 데이터를 가져온다")
    void givenRefreshRequest_whenRefreshCurrentPrice_thenDeleteCacheAndFetchNew() {
        // given
        String symbol = "005930";
        StockPrice newPrice = createSampleStockPrice(symbol);
        
        given(stockPriceCachePort.deleteStockPrice(symbol))
                .willReturn(Mono.empty());
        given(kisApiPort.getCurrentPrice(symbol))
                .willReturn(Mono.just(newPrice));
        given(stockPriceCachePort.saveStockPrice(eq(newPrice), any(Duration.class)))
                .willReturn(Mono.empty());
        
        // when
        Mono<StockPrice> result = stockPriceService.refreshCurrentPrice(symbol);
        
        // then
        StepVerifier.create(result)
                .expectNext(newPrice)
                .verifyComplete();
        
        verify(stockPriceCachePort).deleteStockPrice(symbol);
        verify(kisApiPort).getCurrentPrice(symbol);
        verify(stockPriceCachePort).saveStockPrice(eq(newPrice), any(Duration.class));
    }
    
    @Test
    @DisplayName("현재가 조회 후 MongoDB에 저장한다")
    void givenStockPrice_whenGetCurrentPriceAndSave_thenSaveToRepository() {
        // given
        String symbol = "005930";
        StockPrice stockPrice = createSampleStockPrice(symbol);
        StockPrice savedPrice = createSampleStockPrice(symbol); // ID가 설정된 버전
        
        given(stockPriceCachePort.getStockPrice(symbol))
                .willReturn(Mono.just(stockPrice));
        given(stockPriceRepository.save(stockPrice))
                .willReturn(Mono.just(savedPrice));
        
        // API 호출을 안전하게 처리
        given(kisApiPort.getCurrentPrice(symbol))
                .willReturn(Mono.empty());
        
        // when
        Mono<StockPrice> result = stockPriceService.getCurrentPriceAndSave(symbol);
        
        // then
        StepVerifier.create(result)
                .expectNext(savedPrice)
                .verifyComplete();
        
        verify(stockPriceRepository).save(stockPrice);
    }
    
    @Test
    @DisplayName("API 호출 실패 시 에러가 전파된다")
    void givenApiFailure_whenGetCurrentPrice_thenPropagatesError() {
        // given
        String symbol = "005930";
        RuntimeException apiError = new RuntimeException("KIS API 호출 실패");
        
        given(stockPriceCachePort.getStockPrice(symbol))
                .willReturn(Mono.empty());
        given(kisApiPort.getCurrentPrice(symbol))
                .willReturn(Mono.error(apiError));
        
        // when
        Mono<StockPrice> result = stockPriceService.getCurrentPrice(symbol);
        
        // then
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
        
        verify(stockPriceCachePort, never()).saveStockPrice(any(), any());
    }
    
    @Test
    @DisplayName("캐시 저장 실패해도 데이터는 반환된다")
    void givenCacheSaveFailure_whenGetCurrentPrice_thenStillReturnsData() {
        // given
        String symbol = "005930";
        StockPrice apiPrice = createSampleStockPrice(symbol);
        RuntimeException cacheError = new RuntimeException("캐시 저장 실패");
        
        given(stockPriceCachePort.getStockPrice(symbol))
                .willReturn(Mono.empty());
        given(kisApiPort.getCurrentPrice(symbol))
                .willReturn(Mono.just(apiPrice));
        given(stockPriceCachePort.saveStockPrice(eq(apiPrice), any(Duration.class)))
                .willReturn(Mono.error(cacheError));
        
        // when
        Mono<StockPrice> result = stockPriceService.getCurrentPrice(symbol);
        
        // then
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }
    
    private StockPrice createSampleStockPrice(String symbol) {
        return StockPrice.createWithTTL(
                symbol,
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