package me.rgunny.event.marketdata.infrastructure.adapter.out.kis;

import lombok.extern.slf4j.Slf4j;
import me.rgunny.event.marketdata.application.port.out.kis.KISCredentialPort;
import me.rgunny.event.marketdata.application.port.out.kis.KISTokenCachePort;
import me.rgunny.event.marketdata.application.port.out.shared.StockPort;
import me.rgunny.event.marketdata.domain.error.StockPriceErrorCode;
import me.rgunny.event.marketdata.domain.exception.kis.KisApiException;
import me.rgunny.event.marketdata.domain.model.StockPrice;
import me.rgunny.event.marketdata.infrastructure.config.kis.KISApiProperties;
import me.rgunny.event.marketdata.infrastructure.dto.kis.KISCurrentPriceResponse;
import me.rgunny.event.marketdata.infrastructure.dto.kis.KISCurrentPriceResponseOutput;
import me.rgunny.event.marketdata.infrastructure.dto.kis.KISTokenRequest;
import me.rgunny.event.marketdata.infrastructure.dto.kis.KISTokenResponse;
import me.rgunny.event.marketdata.infrastructure.resilience.KISApiCircuitBreakerService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static me.rgunny.event.marketdata.infrastructure.util.KISFieldParser.toBigDecimal;
import static me.rgunny.event.marketdata.infrastructure.util.KISFieldParser.toLong;

@Slf4j
@Component
public class KISApiAdapter {

    private final WebClient webClient;
    private final KISCredentialPort credentialPort;
    private final KISTokenCachePort tokenCachePort;
    private final StockPort stockPort;
    private final KISApiProperties kisApiProperties;
    private final KISApiCircuitBreakerService circuitBreakerService;
    
    public KISApiAdapter(@Qualifier("kisWebClient") WebClient webClient,
                         KISCredentialPort credentialPort,
                         KISTokenCachePort tokenCachePort,
                         StockPort stockPort,
                         KISApiProperties kisApiProperties,
                         KISApiCircuitBreakerService circuitBreakerService) {
        this.webClient = webClient;
        this.credentialPort = credentialPort;
        this.tokenCachePort = tokenCachePort;
        this.stockPort = stockPort;
        this.kisApiProperties = kisApiProperties;
        this.circuitBreakerService = circuitBreakerService;
        log.info("KISApiAdapter initialized with baseUrl: {} and Circuit Breaker", kisApiProperties.baseUrl());
    }
    
    // KIS OAuth 토큰 유효시간 (24시간)
    private static final Duration TOKEN_TTL = Duration.ofHours(24);

    // API 호출 타임아웃 (10초)
    private static final Duration API_TIMEOUT = Duration.ofSeconds(10);

    // KIS API 전용 메서드들
    public Mono<String> getAccessToken() {
        KISTokenRequest request = new KISTokenRequest(
                kisApiProperties.grantType(),
                credentialPort.getDecryptedAppKey(),
                credentialPort.getDecryptedAppSecret()
        );

        log.info("KIS API Token Request - BaseURL: {}, TokenPath: {}, GrantType: {}, AppKey(masked): {}", 
                kisApiProperties.baseUrl(), 
                kisApiProperties.tokenPath(),
                request.grant_type(),
                credentialPort.getMaskedAppKey());
        
        Mono<String> apiCall = webClient.post()
                .uri(kisApiProperties.tokenPath())
                .header("Content-Type", kisApiProperties.headers().contentType())
                .bodyValue(request)
                .retrieve()
                .onStatus(
                    status -> status.is4xxClientError() || status.is5xxServerError(),
                    response -> response.bodyToMono(String.class)
                        .flatMap(body -> {
                            log.error("KIS API Token Request Failed - Status: {}, Body: {}", 
                                    response.statusCode(), body);
                            return Mono.error(new KisApiException(
                                    StockPriceErrorCode.STOCK_PRICE_005,
                                    "Token Request Failed: " + response.statusCode() + " - " + body));
                        })
                )
                .bodyToMono(KISTokenResponse.class)
                .map(KISTokenResponse::getAccessToken)
                .timeout(API_TIMEOUT)
                .doOnError(error -> log.error("KIS API Token Request Failed", error))
                .doOnSuccess(token -> log.info("KIS API Token received successfully"));
        
        // 서킷브레이커 적용
        return circuitBreakerService.executeGetAccessToken(apiCall);
    }

    public Mono<Boolean> validateConnection() {
        Mono<Boolean> apiCall = getCachedOrNewToken()
                .map(token -> token != null && !token.isEmpty())
                .onErrorReturn(false);
        
        // 서킷브레이커 적용
        return circuitBreakerService.executeValidateConnection(apiCall);
    }

    public Mono<StockPrice> getCurrentPrice(String symbol) {
        Mono<StockPrice> apiCall = getCachedOrNewToken()
                .flatMap(token -> webClient.get()
                        .uri(kisApiProperties.stockPricePath() + "?fid_cond_mrkt_div_code=J&fid_input_iscd={symbol}", symbol)
                        .header("Content-Type", kisApiProperties.headers().contentType())
                        .header("authorization", "Bearer " + token)
                        .header("appkey", credentialPort.getDecryptedAppKey())
                        .header("appsecret", credentialPort.getDecryptedAppSecret())
                        .header(kisApiProperties.headers().transactionId(), kisApiProperties.stockPriceTransactionId())
                        .retrieve()
                        .bodyToMono(KISCurrentPriceResponse.class)
                        .flatMap(response -> mapToStockPriceWithName(symbol, response))
                        .timeout(Duration.ofSeconds(kisApiProperties.timeouts().responseTimeoutSeconds())));
        
        // 서킷브레이커 적용 (캐시 Fallback 포함)
        return circuitBreakerService.executeGetCurrentPrice(symbol, apiCall);
    }

    private Mono<String> getCachedOrNewToken() {
        return tokenCachePort.getToken()
                .filter(token -> token != null && !token.isEmpty())
                .switchIfEmpty(getAccessTokenAndCache())
                .onErrorResume(error -> getAccessTokenAndCache());
    }

    /**
     * 새 토큰 발급 후 캐시에 저장
     */
    private Mono<String> getAccessTokenAndCache() {
        return getAccessToken()
                .flatMap(token ->
                    tokenCachePort.saveToken(token, TOKEN_TTL)
                        .thenReturn(token)
                );
    }

    /**
     * KIS API 응답을 StockPrice 도메인 객체로 변환 (종목명 조회 포함)
     */
    private Mono<StockPrice> mapToStockPriceWithName(String symbol, KISCurrentPriceResponse response) {
        if (response == null || response.output() == null) {
            throw new KisApiException(symbol);
        }

        KISCurrentPriceResponseOutput output = response.output();

        // Stock 엔티티에서 종목명 조회
        return stockPort.findBySymbol(symbol)
                .map(stock -> stock.getName())
                .defaultIfEmpty(symbol) // Stock이 없으면 종목코드를 이름으로 사용
                .map(name -> StockPrice.createWithTTL(
                        symbol,
                        name,
                        toBigDecimal(output.stck_prpr()),           // 현재가
                        toBigDecimal(output.stck_prdy_clpr()),      // 전일종가
                        toBigDecimal(output.stck_hgpr()),           // 고가
                        toBigDecimal(output.stck_lwpr()),           // 저가
                        toBigDecimal(output.stck_oprc()),           // 시가
                        toLong(output.acml_vol()),                  // 누적거래량
                        toBigDecimal(output.askp1()),               // 매도호가1
                        toBigDecimal(output.bidp1())                // 매수호가1
                ));
    }
    
}
