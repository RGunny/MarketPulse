package me.rgunny.marketpulse.event.marketdata.infrastructure.adapter.out.kis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.marketpulse.event.marketdata.application.port.out.kis.KISCredentialPort;
import me.rgunny.marketpulse.event.marketdata.application.port.out.kis.KISTokenPort;
import me.rgunny.marketpulse.event.marketdata.application.port.out.shared.StockPort;
import me.rgunny.marketpulse.event.marketdata.domain.exception.kis.KisApiException;
import me.rgunny.marketpulse.event.marketdata.domain.model.StockPrice;
import me.rgunny.marketpulse.event.marketdata.infrastructure.config.kis.KISApiProperties;
import me.rgunny.marketpulse.event.marketdata.infrastructure.dto.kis.KISCurrentPriceResponse;
import me.rgunny.marketpulse.event.marketdata.infrastructure.dto.kis.KISCurrentPriceResponseOutput;
import me.rgunny.marketpulse.event.marketdata.infrastructure.resilience.KISApiCircuitBreakerService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static me.rgunny.marketpulse.event.marketdata.infrastructure.util.KISFieldParser.toBigDecimal;
import static me.rgunny.marketpulse.event.marketdata.infrastructure.util.KISFieldParser.toLong;

/**
 * KIS API 서비스
 * 
 * KIS API를 통한 시장 데이터 조회 서비스
 * 주가 조회 기능 제공 (토큰 관리는 KISTokenService에 위임)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KISApiService {
    
    @Qualifier("kisWebClient")
    private final WebClient webClient;
    private final KISCredentialPort credentialPort;
    private final KISTokenPort tokenPort;
    private final StockPort stockPort;
    private final KISApiProperties kisApiProperties;
    private final KISApiCircuitBreakerService circuitBreakerService;
    
    // API 호출 타임아웃 (10초)
    private static final Duration API_TIMEOUT = Duration.ofSeconds(10);
    
    /**
     * 현재가 조회 (공개 메서드)
     * 
     * @param symbol 종목 코드
     * @return 주가 정보
     */
    public Mono<StockPrice> fetchCurrentPrice(String symbol) {
        return getCurrentPrice(symbol);
    }
    
    /**
     * 현재가 조회 (내부 메서드)
     */
    private Mono<StockPrice> getCurrentPrice(String symbol) {
        log.debug("Fetching current price for symbol: {}", symbol);
        
        Mono<StockPrice> apiCall = tokenPort.getAccessToken()
                .flatMap(token -> webClient.get()
                        .uri(kisApiProperties.stockPricePath() + 
                             "?fid_cond_mrkt_div_code=J&fid_input_iscd={symbol}", symbol)
                        .header("Content-Type", kisApiProperties.headers().contentType())
                        .header("authorization", "Bearer " + token)
                        .header("appkey", credentialPort.getDecryptedAppKey())
                        .header("appsecret", credentialPort.getDecryptedAppSecret())
                        .header(kisApiProperties.headers().transactionId(), 
                                kisApiProperties.stockPriceTransactionId())
                        .retrieve()
                        .bodyToMono(KISCurrentPriceResponse.class)
                        .flatMap(response -> mapToStockPriceWithName(symbol, response))
                        .timeout(Duration.ofSeconds(
                                kisApiProperties.timeouts().responseTimeoutSeconds())));
        
        // 서킷브레이커 적용
        return circuitBreakerService.executeGetCurrentPrice(symbol, apiCall);
    }
    
    
    /**
     * KIS API 응답을 StockPrice 도메인 객체로 변환
     */
    private Mono<StockPrice> mapToStockPriceWithName(String symbol, KISCurrentPriceResponse response) {
        if (response == null || response.output() == null) {
            log.error("Invalid response for symbol: {}", symbol);
            throw new KisApiException(symbol);
        }

        KISCurrentPriceResponseOutput output = response.output();
        
        // Stock 엔티티에서 종목명 조회
        return stockPort.findBySymbol(symbol)
                .map(stock -> stock.getName())
                .defaultIfEmpty(symbol)
                .map(name -> {
                    StockPrice price = StockPrice.createWithTTL(
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
                    );
                    
                    log.debug("Price data mapped for {}: current={}, change={}%", 
                            symbol, price.getCurrentPrice(), price.getChangeRate());
                    
                    return price;
                });
    }
}