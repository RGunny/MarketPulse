package me.rgunny.marketpulse.event.unit.infrastructure.adapter.output;

import me.rgunny.marketpulse.event.marketdata.domain.model.MarketType;
import me.rgunny.marketpulse.event.marketdata.domain.model.Stock;
import me.rgunny.marketpulse.event.marketdata.infrastructure.adapter.out.shared.StockRepository;
import me.rgunny.marketpulse.event.marketdata.infrastructure.adapter.out.shared.StockRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static reactor.test.StepVerifier.create;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockRepositoryAdapter 단위 테스트")
class StockRepositoryAdapterTest {

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private StockRepositoryAdapter stockRepositoryAdapter;

    private Stock samsungStock;
    private Stock kakaoStock;
    private Stock kodexETF;

    @BeforeEach
    void setUp() {
        samsungStock = Stock.createStock("005930", "삼성전자", "Samsung Electronics",
                MarketType.KOSPI, "33", "전기전자");
        kakaoStock = Stock.createStock("035720", "카카오", "Kakao",
                MarketType.KOSDAQ, "58", "서비스업");
        kodexETF = Stock.createETF("069500", "KODEX 200", "KODEX 200 ETF",
                MarketType.KOSPI);
    }

    @Nested
    @DisplayName("저장 기능")
    class SaveTest {

        @Test
        @DisplayName("종목 정보 저장 시 정상적으로 저장된다")
        void givenStock_whenSave_thenReturnsSavedStock() {
            // given
            given(stockRepository.save(any(Stock.class))).willReturn(Mono.just(samsungStock));

            // when
            Mono<Stock> result = stockRepositoryAdapter.save(samsungStock);

            // then
            create(result)
                    .expectNext(samsungStock)
                    .verifyComplete();

            verify(stockRepository, times(1)).save(samsungStock);
        }

        @Test
        @DisplayName("종목 정보 저장 실패 시 에러를 반환한다")
        void givenStock_whenSaveFails_thenReturnsError() {
            // given
            RuntimeException exception = new RuntimeException("DB connection failed");
            given(stockRepository.save(any(Stock.class))).willReturn(Mono.error(exception));

            // when
            Mono<Stock> result = stockRepositoryAdapter.save(samsungStock);

            // then
            create(result)
                    .expectError(RuntimeException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("조회 기능")
    class FindTest {

        @Test
        @DisplayName("종목코드로 조회 시 해당 종목을 반환한다")
        void givenSymbol_whenFindBySymbol_thenReturnsStock() {
            // given
            String symbol = "005930";
            given(stockRepository.findBySymbol(symbol)).willReturn(Mono.just(samsungStock));

            // when
            Mono<Stock> result = stockRepositoryAdapter.findBySymbol(symbol);

            // then
            create(result)
                    .expectNext(samsungStock)
                    .verifyComplete();

            verify(stockRepository, times(1)).findBySymbol(symbol);
        }

        @Test
        @DisplayName("존재하지 않는 종목코드로 조회 시 빈 값을 반환한다")
        void givenNonExistentSymbol_whenFindBySymbol_thenReturnsEmpty() {
            // given
            String symbol = "999999";
            given(stockRepository.findBySymbol(symbol)).willReturn(Mono.empty());

            // when
            Mono<Stock> result = stockRepositoryAdapter.findBySymbol(symbol);

            // then
            create(result)
                    .verifyComplete();
        }

        @Test
        @DisplayName("종목명 부분 일치로 검색 시 해당하는 종목들을 반환한다")
        void givenPartialName_whenFindByNameContaining_thenReturnsMatchingStocks() {
            // given
            String partialName = "삼성";
            given(stockRepository.findByNameContaining(partialName))
                    .willReturn(Flux.just(samsungStock));

            // when
            Flux<Stock> result = stockRepositoryAdapter.findByNameContaining(partialName);

            // then
            create(result)
                    .expectNext(samsungStock)
                    .verifyComplete();
        }

        @Test
        @DisplayName("활성 종목 조회 시 활성 상태인 종목들만 반환한다")
        void whenFindAllActiveStocks_thenReturnsOnlyActiveStocks() {
            // given
            given(stockRepository.findByIsActiveTrue())
                    .willReturn(Flux.just(samsungStock, kakaoStock));

            // when
            Flux<Stock> result = stockRepositoryAdapter.findAllActiveStocks();

            // then
            create(result)
                    .expectNext(samsungStock)
                    .expectNext(kakaoStock)
                    .verifyComplete();
        }

        @Test
        @DisplayName("시장별 활성 종목 조회 시 해당 시장의 활성 종목만 반환한다")
        void givenMarketType_whenFindActiveStocksByMarket_thenReturnsMarketSpecificActiveStocks() {
            // given
            MarketType marketType = MarketType.KOSPI;
            given(stockRepository.findByMarketTypeAndIsActive(marketType, true))
                    .willReturn(Flux.just(samsungStock, kodexETF));

            // when
            Flux<Stock> result = stockRepositoryAdapter.findActiveStocksByMarket(marketType);

            // then
            create(result)
                    .expectNext(samsungStock)
                    .expectNext(kodexETF)
                    .verifyComplete();
        }

        @Test
        @DisplayName("ETF 목록 조회 시 ETF만 반환한다")
        void whenFindAllETFs_thenReturnsOnlyETFs() {
            // given
            given(stockRepository.findByIsETFTrue()).willReturn(Flux.just(kodexETF));

            // when
            Flux<Stock> result = stockRepositoryAdapter.findAllETFs();

            // then
            create(result)
                    .expectNext(kodexETF)
                    .verifyComplete();
        }

        @Test
        @DisplayName("업종별 종목 조회 시 해당 업종의 종목들을 반환한다")
        void givenSectorCode_whenFindBySector_thenReturnsSectorSpecificStocks() {
            // given
            String sectorCode = "33";
            given(stockRepository.findBySectorCode(sectorCode))
                    .willReturn(Flux.just(samsungStock));

            // when
            Flux<Stock> result = stockRepositoryAdapter.findBySector(sectorCode);

            // then
            create(result)
                    .expectNext(samsungStock)
                    .verifyComplete();
        }

        @Test
        @DisplayName("여러 종목코드로 조회 시 해당하는 종목들을 반환한다")
        void givenMultipleSymbols_whenFindBySymbols_thenReturnsMatchingStocks() {
            // given
            Flux<String> symbols = Flux.just("005930", "035720");
            given(stockRepository.findBySymbolIn(symbols))
                    .willReturn(Flux.just(samsungStock, kakaoStock));

            // when
            Flux<Stock> result = stockRepositoryAdapter.findBySymbols(symbols);

            // then
            create(result)
                    .expectNext(samsungStock)
                    .expectNext(kakaoStock)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("존재 여부 확인")
    class ExistsTest {

        @Test
        @DisplayName("종목코드가 존재하면 true를 반환한다")
        void givenExistingSymbol_whenExistsBySymbol_thenReturnsTrue() {
            // given
            String symbol = "005930";
            given(stockRepository.existsBySymbol(symbol)).willReturn(Mono.just(true));

            // when
            Mono<Boolean> result = stockRepositoryAdapter.existsBySymbol(symbol);

            // then
            create(result)
                    .expectNext(true)
                    .verifyComplete();
        }

        @Test
        @DisplayName("종목코드가 존재하지 않으면 false를 반환한다")
        void givenNonExistentSymbol_whenExistsBySymbol_thenReturnsFalse() {
            // given
            String symbol = "999999";
            given(stockRepository.existsBySymbol(symbol)).willReturn(Mono.just(false));

            // when
            Mono<Boolean> result = stockRepositoryAdapter.existsBySymbol(symbol);

            // then
            create(result)
                    .expectNext(false)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("업데이트 기능")
    class UpdateTest {

        @Test
        @DisplayName("종목 정보 업데이트 시 정상적으로 업데이트된다")
        void givenStock_whenUpdate_thenReturnsUpdatedStock() {
            // given
            Stock updatedStock = samsungStock.updateInfo("삼성전자우", "34", "전기전자우선주", true);
            given(stockRepository.save(any(Stock.class))).willReturn(Mono.just(updatedStock));

            // when
            Mono<Stock> result = stockRepositoryAdapter.save(updatedStock);

            // then
            create(result)
                    .expectNext(updatedStock)
                    .verifyComplete();

            verify(stockRepository, times(1)).save(updatedStock);
        }
    }

    @Nested
    @DisplayName("삭제 기능")
    class DeleteTest {

        @Test
        @DisplayName("종목코드로 삭제 시 해당 종목이 삭제된다")
        void givenSymbol_whenDeleteBySymbol_thenDeletesStock() {
            // given
            String symbol = "005930";
            given(stockRepository.findBySymbol(symbol)).willReturn(Mono.just(samsungStock));
            given(stockRepository.delete(samsungStock)).willReturn(Mono.empty());

            // when
            Mono<Void> result = stockRepositoryAdapter.deleteBySymbol(symbol);

            // then
            create(result)
                    .verifyComplete();

            verify(stockRepository, times(1)).findBySymbol(symbol);
            verify(stockRepository, times(1)).delete(samsungStock);
        }

        @Test
        @DisplayName("존재하지 않는 종목 삭제 시도 시 아무 동작 없이 완료된다")
        void givenNonExistentSymbol_whenDeleteBySymbol_thenCompletesWithoutError() {
            // given
            String symbol = "999999";
            given(stockRepository.findBySymbol(symbol)).willReturn(Mono.empty());

            // when
            Mono<Void> result = stockRepositoryAdapter.deleteBySymbol(symbol);

            // then
            create(result)
                    .verifyComplete();

            verify(stockRepository, times(1)).findBySymbol(symbol);
            verify(stockRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("카운트 기능")
    class CountTest {

        @Test
        @DisplayName("전체 종목 수를 반환한다")
        void whenCount_thenReturnsTotalCount() {
            // given
            given(stockRepository.count()).willReturn(Mono.just(100L));

            // when
            Mono<Long> result = stockRepositoryAdapter.count();

            // then
            create(result)
                    .expectNext(100L)
                    .verifyComplete();
        }

        @Test
        @DisplayName("활성 종목 수를 반환한다")
        void whenCountActiveStocks_thenReturnsActiveCount() {
            // given
            given(stockRepository.findByIsActiveTrue())
                    .willReturn(Flux.just(samsungStock, kakaoStock, kodexETF));

            // when
            Mono<Long> result = stockRepositoryAdapter.countActiveStocks();

            // then
            create(result)
                    .expectNext(3L)
                    .verifyComplete();
        }
    }
}