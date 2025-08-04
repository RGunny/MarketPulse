package me.rgunny.event.marketdata.infrastructure.adapter.out.kis;

import lombok.extern.slf4j.Slf4j;
import me.rgunny.event.marketdata.application.port.out.ExternalApiPort;
import me.rgunny.event.marketdata.application.port.out.kis.KISCredentialPort;
import me.rgunny.event.marketdata.application.port.out.kis.KISTokenCachePort;
import me.rgunny.event.marketdata.domain.exception.kis.KisApiException;
import me.rgunny.event.marketdata.domain.model.StockPrice;
import me.rgunny.event.marketdata.infrastructure.config.kis.KISApiProperties;
import me.rgunny.event.marketdata.infrastructure.dto.kis.KISCurrentPriceResponse;
import me.rgunny.event.marketdata.infrastructure.dto.kis.KISCurrentPriceResponseOutput;
import me.rgunny.event.marketdata.infrastructure.dto.kis.KISTokenRequest;
import me.rgunny.event.marketdata.infrastructure.dto.kis.KISTokenResponse;
import me.rgunny.event.shared.domain.value.MarketDataType;
import me.rgunny.event.shared.domain.value.MarketDataValue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
public class KISApiAdapter implements ExternalApiPort {

    private final WebClient webClient;
    private final KISCredentialPort credentialPort;
    private final KISTokenCachePort tokenCachePort;
    private final KISApiProperties kisApiProperties;
    
    public KISApiAdapter(@Qualifier("kisWebClient") WebClient webClient,
                         KISCredentialPort credentialPort,
                         KISTokenCachePort tokenCachePort,
                         KISApiProperties kisApiProperties) {
        this.webClient = webClient;
        this.credentialPort = credentialPort;
        this.tokenCachePort = tokenCachePort;
        this.kisApiProperties = kisApiProperties;
        log.info("KISApiAdapter initialized with baseUrl: {}", kisApiProperties.baseUrl());
    }
    
    // KIS OAuth 토큰 유효시간 (24시간)
    private static final Duration TOKEN_TTL = Duration.ofHours(24);

    @Override
    public boolean supports(MarketDataType dataType) {
        return dataType == MarketDataType.STOCK;
    }
    
    @Override
    public String getProviderName() {
        return "KIS";
    }
    
    @Override
    public int getRateLimitPerMinute() {
        return 200; // KIS API 제한
    }
    
    @Override
    public <T extends MarketDataValue> Mono<T> fetchMarketData(String symbol, MarketDataType dataType, Class<T> valueType) {
        if (!supports(dataType)) {
            return Mono.error(new IllegalArgumentException("Unsupported data type: " + dataType));
        }
        
        if (dataType == MarketDataType.STOCK && valueType.isAssignableFrom(StockPrice.class)) {
            return getCurrentPrice(symbol).cast(valueType);
        }
        
        return Mono.error(new IllegalArgumentException("Unsupported value type: " + valueType));
    }
    
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
        
        return webClient.post()
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
                            return Mono.error(new RuntimeException(
                                    "KIS API Error: " + response.statusCode() + " - " + body));
                        })
                )
                .bodyToMono(KISTokenResponse.class)
                .map(KISTokenResponse::getAccessToken)
                .timeout(Duration.ofSeconds(10))
                .doOnError(error -> log.error("KIS API Token Request Failed", error))
                .doOnSuccess(token -> log.info("KIS API Token received successfully"));
    }

    public Mono<String> getCachedOrNewToken() {
        return tokenCachePort.getToken()
                .filter(token -> token != null && !token.isEmpty())
                .switchIfEmpty(getAccessTokenAndCache())
                .onErrorResume(error -> getAccessTokenAndCache());
    }

    public Mono<Boolean> validateConnection() {
        return getCachedOrNewToken()
                .map(token -> token != null && !token.isEmpty())
                .onErrorReturn(false);
    }

    public Mono<StockPrice> getCurrentPrice(String symbol) {
        return getCachedOrNewToken()
                .flatMap(token -> webClient.get()
                        .uri(kisApiProperties.stockPricePath() + "?fid_cond_mrkt_div_code=J&fid_input_iscd={symbol}", symbol)
                        .header("Content-Type", kisApiProperties.headers().contentType())
                        .header("authorization", "Bearer " + token)
                        .header("appkey", credentialPort.getDecryptedAppKey())
                        .header("appsecret", credentialPort.getDecryptedAppSecret())
                        .header(kisApiProperties.headers().transactionId(), kisApiProperties.stockPriceTransactionId())
                        .retrieve()
                        .bodyToMono(KISCurrentPriceResponse.class)
                        .map(response -> mapToStockPrice(symbol, response))
                        .timeout(Duration.ofSeconds(kisApiProperties.timeouts().responseTimeoutSeconds())));
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
     * KIS API 응답을 StockPrice 도메인 객체로 변환
     */
    private StockPrice mapToStockPrice(String symbol, KISCurrentPriceResponse response) {
        if (response == null || response.output() == null) {
            throw new KisApiException(symbol);
        }
        
        KISCurrentPriceResponseOutput output = response.output();
        
        // null 체크 및 기본값 처리
        String name = symbol; // TODO: 종목마스터 테이블에서 조회하도록 개선
        
        return StockPrice.createWithTTL(
                symbol,
                name,
                safeParseBigDecimal(output.stck_prpr()),           // 현재가
                safeParseBigDecimal(output.stck_prdy_clpr()),      // 전일종가
                safeParseBigDecimal(output.stck_hgpr()),           // 고가
                safeParseBigDecimal(output.stck_lwpr()),           // 저가
                safeParseBigDecimal(output.stck_oprc()),           // 시가
                safeParseLong(output.acml_vol()),                  // 누적거래량
                safeParseBigDecimal(output.askp1()),               // 매도호가1
                safeParseBigDecimal(output.bidp1())                // 매수호가1
        );
    }
    
    /**
     * 안전한 BigDecimal 변환
     */
    private BigDecimal safeParseBigDecimal(String value) {
        return Optional.ofNullable(value)
                .filter(v -> !v.isBlank())
                .map(BigDecimal::new)
                .orElse(BigDecimal.ZERO);
    }
    
    /**
     * 안전한 Long 변환
     */
    private Long safeParseLong(String value) {
        return Optional.ofNullable(value)
                .filter(v -> !v.isBlank())
                .map(Long::parseLong)
                .orElse(0L);
    }

}
