package me.rgunny.event.unit.infrastructure.adapter.output;

import me.rgunny.event.application.port.output.KISCredentialPort;
import me.rgunny.event.application.port.output.KISTokenCachePort;
import me.rgunny.event.domain.stock.StockPrice;
import me.rgunny.event.infrastructure.adapter.output.KISApiAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;

/**
 * KISApiAdapter 단위 테스트 - 최신 실무 스타일
 * 
 * Spring WebFlux 공식 권장 테스트 방법 사용
 * - ExchangeFunction을 통한 WebClient 테스트
 * - 비즈니스 로직 수정 없음
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("KISApiAdapter - 토큰 관리 (unit)")
class KISApiAdapterTest {

    @Mock private KISCredentialPort credentialPort;
    @Mock private KISTokenCachePort tokenCachePort;
    @Mock private ExchangeFunction exchangeFunction;

    private KISApiAdapter kisApiAdapter;

    // 테스트 상수
    private static final String TEST_APP_KEY = "test-app-key";
    private static final String TEST_APP_SECRET = "test-app-secret";
    private static final String TEST_TOKEN = "test-access-token";
    private static final Duration TOKEN_TTL = Duration.ofHours(24);

    @BeforeEach
    void setUp() {
        // ExchangeFunction을 사용한 WebClient 생성
        WebClient webClient = WebClient.builder()
                .baseUrl("https://openapi.koreainvestment.com:9443")
                .exchangeFunction(exchangeFunction)
                .build();
        
        kisApiAdapter = new KISApiAdapter(webClient, credentialPort, tokenCachePort);
        
        // 기본 credential 설정
        given(credentialPort.getDecryptedAppKey()).willReturn(TEST_APP_KEY);
        given(credentialPort.getDecryptedAppSecret()).willReturn(TEST_APP_SECRET);
    }

    @Nested
    @DisplayName("getCachedOrNewToken() - 캐시 우선 전략")
    class GetCachedOrNewTokenTests {

        @Test
        @DisplayName("캐시 히트 시 API 호출을 하지 않는다")
        void givenCachedToken_whenGetCachedOrNewToken_thenReturnsCachedToken() {
            // given
            String cachedToken = "cached-valid-token";
            given(tokenCachePort.getToken()).willReturn(Mono.just(cachedToken));

            // when
            Mono<String> result = kisApiAdapter.getCachedOrNewToken();

            // then
            StepVerifier.create(result)
                    .expectNext(cachedToken)
                    .verifyComplete();

            then(tokenCachePort).should(times(1)).getToken();
            then(exchangeFunction).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("캐시 미스 시 새 토큰을 발급하고 저장한다")
        void givenEmptyCache_whenGetCachedOrNewToken_thenFetchesNewToken() {
            // given
            given(tokenCachePort.getToken()).willReturn(Mono.empty());
            given(tokenCachePort.saveToken(TEST_TOKEN, TOKEN_TTL)).willReturn(Mono.empty());
            
            setupSuccessfulTokenResponse();

            // when
            Mono<String> result = kisApiAdapter.getCachedOrNewToken();

            // then
            StepVerifier.create(result)
                    .expectNext(TEST_TOKEN)
                    .verifyComplete();

            then(tokenCachePort).should().getToken();
            then(tokenCachePort).should().saveToken(TEST_TOKEN, TOKEN_TTL);
            then(exchangeFunction).should().exchange(any(ClientRequest.class));
        }

        @Test
        @DisplayName("캐시 에러 시 새 토큰 발급으로 복구한다")
        void givenCacheError_whenGetCachedOrNewToken_thenRecoverWithNewToken() {
            // given
            given(tokenCachePort.getToken())
                    .willReturn(Mono.error(new RuntimeException("Cache unavailable")));
            given(tokenCachePort.saveToken(TEST_TOKEN, TOKEN_TTL)).willReturn(Mono.empty());
            
            setupSuccessfulTokenResponse();

            // when
            Mono<String> result = kisApiAdapter.getCachedOrNewToken();

            // then
            StepVerifier.create(result)
                    .expectNext(TEST_TOKEN)
                    .verifyComplete();

            then(exchangeFunction).should().exchange(any(ClientRequest.class));
        }

        @Test
        @DisplayName("모든 시도가 실패하면 에러를 전파한다")
        void givenAllFailures_whenGetCachedOrNewToken_thenPropagatesError() {
            // given
            given(tokenCachePort.getToken()).willReturn(Mono.empty());
            setupFailedTokenResponse();

            // when
            Mono<String> result = kisApiAdapter.getCachedOrNewToken();

            // then
            StepVerifier.create(result)
                    .expectError()
                    .verify();

            then(tokenCachePort).should(never()).saveToken(anyString(), any(Duration.class));
        }
    }

    @Nested
    @DisplayName("validateConnection() - 연결 상태 검증")
    class ValidateConnectionTests {

        @Test
        @DisplayName("유효한 토큰이 있으면 true를 반환한다")
        void givenValidToken_whenValidateConnection_thenReturnsTrue() {
            // given
            given(tokenCachePort.getToken()).willReturn(Mono.just("valid-token"));

            // when
            Mono<Boolean> result = kisApiAdapter.validateConnection();

            // then
            StepVerifier.create(result)
                    .expectNext(true)
                    .verifyComplete();

            then(exchangeFunction).shouldHaveNoInteractions();
        }

        @Test
        @DisplayName("빈 문자열 토큰은 새 토큰 발급을 시도한다")
        void givenEmptyStringToken_whenValidateConnection_thenFetchesNewToken() {
            // given
            given(tokenCachePort.getToken()).willReturn(Mono.just(""));
            given(tokenCachePort.saveToken(TEST_TOKEN, TOKEN_TTL)).willReturn(Mono.empty());
            
            setupSuccessfulTokenResponse();

            // when
            Mono<Boolean> result = kisApiAdapter.validateConnection();

            // then
            StepVerifier.create(result)
                    .expectNext(true)
                    .verifyComplete();

            then(exchangeFunction).should().exchange(any(ClientRequest.class));
        }

        @Test
        @DisplayName("토큰 획득 실패 시 false를 반환한다")
        void givenTokenFailure_whenValidateConnection_thenReturnsFalse() {
            // given
            given(tokenCachePort.getToken()).willReturn(Mono.empty());
            setupFailedTokenResponse();

            // when
            Mono<Boolean> result = kisApiAdapter.validateConnection();

            // then
            StepVerifier.create(result)
                    .expectNext(false)
                    .verifyComplete();
        }
    }

    @Nested
    @DisplayName("getAccessToken() - 직접 API 호출")
    class GetAccessTokenTests {

        @Test
        @DisplayName("성공적으로 토큰을 발급받는다")
        void givenValidCredentials_whenGetAccessToken_thenReturnsToken() {
            // given
            setupSuccessfulTokenResponse();

            // when
            Mono<String> result = kisApiAdapter.getAccessToken();

            // then
            StepVerifier.create(result)
                    .expectNext(TEST_TOKEN)
                    .verifyComplete();

            then(credentialPort).should().getDecryptedAppKey();
            then(credentialPort).should().getDecryptedAppSecret();
        }

        @Test
        @DisplayName("API 에러를 적절히 처리한다")
        void givenApiError_whenGetAccessToken_thenHandlesError() {
            // given
            setupFailedTokenResponse();

            // when
            Mono<String> result = kisApiAdapter.getAccessToken();

            // then
            StepVerifier.create(result)
                    .expectError()
                    .verify();
        }

        @Test
        @DisplayName("타임아웃이 적용된다")
        void givenSlowResponse_whenGetAccessToken_thenTimesOut() {
            // given
            setupDelayedTokenResponse();

            // when
            Mono<String> result = kisApiAdapter.getAccessToken();

            // then
            // Virtual time을 사용해 타임아웃 검증
            StepVerifier.withVirtualTime(() -> result)
                    .thenAwait(Duration.ofSeconds(11))
                    .expectError()
                    .verify();
        }
    }

    @Nested
    @DisplayName("getCurrentPrice() - 종목 현재가 조회")
    class GetCurrentPriceTests {

        @Test
        @DisplayName("성공적으로 현재가를 조회하고 StockPrice로 매핑한다")
        void givenValidSymbol_whenGetCurrentPrice_thenReturnsStockPrice() {
            // given
            String symbol = "005930";
            given(tokenCachePort.getToken()).willReturn(Mono.just(TEST_TOKEN));
            setupSuccessfulCurrentPriceResponse();

            // when
            Mono<StockPrice> result = kisApiAdapter.getCurrentPrice(symbol);

            // then
            StepVerifier.create(result)
                    .assertNext(stockPrice -> {
                        assertThat(stockPrice.getSymbol()).isEqualTo(symbol);
                        assertThat(stockPrice.getCurrentPrice()).isEqualByComparingTo(new BigDecimal("71000"));
                        assertThat(stockPrice.getPreviousClose()).isEqualByComparingTo(new BigDecimal("70000"));
                        assertThat(stockPrice.getHigh()).isEqualByComparingTo(new BigDecimal("71500"));
                        assertThat(stockPrice.getLow()).isEqualByComparingTo(new BigDecimal("70500"));
                        assertThat(stockPrice.getOpen()).isEqualByComparingTo(new BigDecimal("70800"));
                        assertThat(stockPrice.getVolume()).isEqualTo(1000000L);
                        assertThat(stockPrice.getAskPrice1()).isEqualByComparingTo(new BigDecimal("71100"));
                        assertThat(stockPrice.getBidPrice1()).isEqualByComparingTo(new BigDecimal("70900"));
                    })
                    .verifyComplete();

            then(exchangeFunction).should().exchange(any(ClientRequest.class));
        }

        @Test
        @DisplayName("토큰이 없으면 새로 발급받아 API 호출한다")
        void givenNoToken_whenGetCurrentPrice_thenFetchesTokenAndCallsApi() {
            // given
            String symbol = "005930";
            given(tokenCachePort.getToken())
                    .willReturn(Mono.empty()) // 첫 번째 호출: 캐시 미스
                    .willReturn(Mono.just(TEST_TOKEN)); // 두 번째 호출: 토큰 저장 후
            given(tokenCachePort.saveToken(TEST_TOKEN, TOKEN_TTL)).willReturn(Mono.empty());
            
            // 토큰 발급 응답과 현재가 조회 응답을 순서대로 설정
            setupSequentialResponses();

            // when
            Mono<StockPrice> result = kisApiAdapter.getCurrentPrice(symbol);

            // then
            StepVerifier.create(result)
                    .assertNext(stockPrice -> {
                        assertThat(stockPrice.getSymbol()).isEqualTo(symbol);
                        assertThat(stockPrice.getCurrentPrice()).isEqualByComparingTo(new BigDecimal("71000"));
                    })
                    .verifyComplete();

            // 토큰 발급과 현재가 조회 모두 호출되어야 함
            then(exchangeFunction).should(times(2)).exchange(any(ClientRequest.class));
        }

        @Test
        @DisplayName("API 에러를 적절히 처리한다")
        void givenApiError_whenGetCurrentPrice_thenHandlesError() {
            // given
            String symbol = "005930";
            given(tokenCachePort.getToken()).willReturn(Mono.just(TEST_TOKEN));
            setupFailedCurrentPriceResponse();

            // when
            Mono<StockPrice> result = kisApiAdapter.getCurrentPrice(symbol);

            // then
            StepVerifier.create(result)
                    .expectError()
                    .verify();
        }

        @Test
        @DisplayName("타임아웃이 적용된다")
        void givenSlowResponse_whenGetCurrentPrice_thenTimesOut() {
            // given
            String symbol = "005930";
            given(tokenCachePort.getToken()).willReturn(Mono.just(TEST_TOKEN));
            setupDelayedCurrentPriceResponse();

            // when
            Mono<StockPrice> result = kisApiAdapter.getCurrentPrice(symbol);

            // then
            StepVerifier.withVirtualTime(() -> result)
                    .thenAwait(Duration.ofSeconds(11))
                    .expectError()
                    .verify();
        }
    }

    // === Helper Methods ===

    private void setupSuccessfulTokenResponse() {
        String responseBody = """
            {
                "access_token": "%s",
                "token_type": "Bearer",
                "expires_in": 86400
            }
            """.formatted(TEST_TOKEN);

        ClientResponse mockResponse = ClientResponse.create(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(responseBody)
                .build();

        given(exchangeFunction.exchange(any(ClientRequest.class)))
                .willReturn(Mono.just(mockResponse));
    }

    private void setupFailedTokenResponse() {
        ClientResponse mockResponse = ClientResponse.create(HttpStatus.UNAUTHORIZED)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body("{\"error\":\"invalid_client\"}")
                .build();

        given(exchangeFunction.exchange(any(ClientRequest.class)))
                .willReturn(Mono.just(mockResponse));
    }

    private void setupDelayedTokenResponse() {
        String responseBody = """
            {
                "access_token": "%s",
                "token_type": "Bearer",
                "expires_in": 86400
            }
            """.formatted(TEST_TOKEN);

        ClientResponse mockResponse = ClientResponse.create(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(responseBody)
                .build();

        // 11초 지연 후 응답
        given(exchangeFunction.exchange(any(ClientRequest.class)))
                .willReturn(Mono.just(mockResponse).delayElement(Duration.ofSeconds(11)));
    }

    private void setupSuccessfulCurrentPriceResponse() {
        String responseBody = """
            {
                "rt_cd": "0",
                "msg_cd": "AAPL000000",
                "msg1": "정상처리되었습니다.",
                "output": {
                    "stck_prpr": "71000",
                    "prdy_vrss": "1000",
                    "prdy_vrss_sign": "2",
                    "prdy_ctrt": "1.43",
                    "stck_prdy_clpr": "70000",
                    "acml_vol": "1000000",
                    "askp1": "71100",
                    "bidp1": "70900",
                    "stck_hgpr": "71500",
                    "stck_lwpr": "70500",
                    "stck_oprc": "70800"
                }
            }
            """;

        ClientResponse mockResponse = ClientResponse.create(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(responseBody)
                .build();

        given(exchangeFunction.exchange(any(ClientRequest.class)))
                .willReturn(Mono.just(mockResponse));
    }

    private void setupFailedCurrentPriceResponse() {
        ClientResponse mockResponse = ClientResponse.create(HttpStatus.BAD_REQUEST)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body("{\"rt_cd\":\"1\",\"msg1\":\"잘못된 종목코드입니다\"}")
                .build();

        given(exchangeFunction.exchange(any(ClientRequest.class)))
                .willReturn(Mono.just(mockResponse));
    }

    private void setupDelayedCurrentPriceResponse() {
        String responseBody = """
            {
                "rt_cd": "0",
                "msg_cd": "AAPL000000",
                "msg1": "정상처리되었습니다.",
                "output": {
                    "stck_prpr": "71000",
                    "prdy_vrss": "1000",
                    "prdy_vrss_sign": "2",
                    "prdy_ctrt": "1.43",
                    "stck_prdy_clpr": "70000",
                    "acml_vol": "1000000",
                    "askp1": "71100",
                    "bidp1": "70900",
                    "stck_hgpr": "71500",
                    "stck_lwpr": "70500",
                    "stck_oprc": "70800"
                }
            }
            """;

        ClientResponse mockResponse = ClientResponse.create(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(responseBody)
                .build();

        // 11초 지연 후 응답
        given(exchangeFunction.exchange(any(ClientRequest.class)))
                .willReturn(Mono.just(mockResponse).delayElement(Duration.ofSeconds(11)));
    }

    private void setupSequentialResponses() {
        // 토큰 발급 응답
        String tokenResponseBody = """
            {
                "access_token": "%s",
                "token_type": "Bearer",
                "expires_in": 86400
            }
            """.formatted(TEST_TOKEN);

        ClientResponse tokenResponse = ClientResponse.create(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(tokenResponseBody)
                .build();

        // 현재가 조회 응답
        String priceResponseBody = """
            {
                "rt_cd": "0",
                "msg_cd": "AAPL000000",
                "msg1": "정상처리되었습니다.",
                "output": {
                    "stck_prpr": "71000",
                    "prdy_vrss": "1000",
                    "prdy_vrss_sign": "2",
                    "prdy_ctrt": "1.43",
                    "stck_prdy_clpr": "70000",
                    "acml_vol": "1000000",
                    "askp1": "71100",
                    "bidp1": "70900",
                    "stck_hgpr": "71500",
                    "stck_lwpr": "70500",
                    "stck_oprc": "70800"
                }
            }
            """;

        ClientResponse priceResponse = ClientResponse.create(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(priceResponseBody)
                .build();

        // 순차적으로 다른 응답 반환
        given(exchangeFunction.exchange(any(ClientRequest.class)))
                .willReturn(Mono.just(tokenResponse))     // 첫 번째 호출: 토큰 발급
                .willReturn(Mono.just(priceResponse));    // 두 번째 호출: 현재가 조회
    }
}
