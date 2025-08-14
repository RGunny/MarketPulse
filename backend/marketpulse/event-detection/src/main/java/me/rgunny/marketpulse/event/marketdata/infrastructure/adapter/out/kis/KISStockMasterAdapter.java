package me.rgunny.marketpulse.event.marketdata.infrastructure.adapter.out.kis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.marketpulse.event.marketdata.application.port.out.StockMasterPort;
import me.rgunny.marketpulse.event.marketdata.application.port.out.kis.KISCredentialPort;
import me.rgunny.marketpulse.event.marketdata.application.port.out.kis.KISTokenPort;
import me.rgunny.marketpulse.event.marketdata.domain.model.MarketType;
import me.rgunny.marketpulse.event.marketdata.domain.model.Stock;
import me.rgunny.marketpulse.event.marketdata.infrastructure.config.kis.KISApiProperties;
import me.rgunny.marketpulse.event.marketdata.infrastructure.dto.kis.KISStockMasterResponse;
import me.rgunny.marketpulse.event.marketdata.infrastructure.dto.kis.KISStockMasterResponse.StockMasterOutput;
import me.rgunny.marketpulse.event.marketdata.infrastructure.resilience.KISApiCircuitBreakerService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * KIS API 종목 마스터 어댑터
 * 
 * 종목 마스터 데이터 조회 전용
 * 토큰 관리는 KISTokenPort를 통해 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KISStockMasterAdapter implements StockMasterPort {
    
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);
    private static final int MAX_RETRY_ATTEMPTS = 3;
    
    @Qualifier("kisWebClient")
    private final WebClient webClient;
    private final KISApiProperties kisApiProperties;
    private final KISApiCircuitBreakerService circuitBreakerService;
    private final KISCredentialPort credentialPort;
    private final KISTokenPort tokenPort; // 토큰 관리 포트
    
    @Override
    public Flux<Stock> fetchAllStocks() {
        log.info("전체 종목 마스터 조회 시작");
        
        return Flux.merge(
                fetchStocksByMarket("KOSPI"),
                fetchStocksByMarket("KOSDAQ")
        )
        .doOnComplete(() -> log.info("전체 종목 마스터 조회 완료"))
        .doOnError(error -> log.error("전체 종목 마스터 조회 실패", error));
    }
    
    @Override
    public Flux<Stock> fetchStocksByMarket(String market) {
        log.info("시장별 종목 마스터 조회 시작: market={}", market);
        
        return circuitBreakerService.executeWithFallback(
                "kis-stock-master",
                () -> fetchStockMasterFromAPI(market),
                throwable -> {
                    log.error("종목 마스터 조회 실패, 폴백 실행: market={}", market, throwable);
                    return Flux.empty();
                }
        );
    }
    
    @Override
    public Mono<Stock> fetchStockDetail(String symbol) {
        log.info("종목 상세 조회: symbol={}", symbol);
        
        return circuitBreakerService.executeMonoWithFallback(
                "kis-stock-detail",
                () -> fetchStockDetailFromAPI(symbol),
                throwable -> {
                    log.error("종목 상세 조회 실패: symbol={}", symbol, throwable);
                    return Mono.empty();
                }
        );
    }
    
    @Override
    public Mono<String> getLastUpdateTime() {
        return Mono.just(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
    
    private Flux<Stock> fetchStockMasterFromAPI(String market) {
        // 토큰은 KISTokenPort를 통해 획득
        return tokenPort.getAccessToken()
                .flatMapMany(token -> 
                    webClient.get()
                            .uri(uriBuilder -> uriBuilder
                                    .path(kisApiProperties.stockMasterPath())
                                    .queryParam("PRDT_TYPE_CD", getProductTypeCode(market))
                                    .build())
                            .header("Content-Type", kisApiProperties.headers().contentType())
                            .header("authorization", "Bearer " + token)
                            .header("appkey", credentialPort.getDecryptedAppKey())
                            .header("appsecret", credentialPort.getDecryptedAppSecret())
                            .header(kisApiProperties.headers().transactionId(), 
                                    kisApiProperties.stockMasterTransactionId())
                            .retrieve()
                            .bodyToMono(KISStockMasterResponse.class)
                            .timeout(REQUEST_TIMEOUT)
                            .retryWhen(Retry.backoff(MAX_RETRY_ATTEMPTS, Duration.ofSeconds(1)))
                            .flatMapMany(response -> {
                                if (response.isSuccess() && response.output() != null) {
                                    // 실무: 대량 데이터는 스트리밍으로 처리
                                    return Flux.fromStream(response.output().stream())
                                            .map(this::convertToStock)
                                            .filter(stock -> stock != null)
                                            .onBackpressureBuffer(1000); // 백프레셔 제어
                                } else {
                                    log.error("종목 마스터 조회 실패: code={}, msg={}", 
                                            response.returnCode(), response.message());
                                    return Flux.empty();
                                }
                            })
                )
                .doOnError(error -> log.error("종목 마스터 API 호출 실패: market={}", market, error));
    }
    
    private Mono<Stock> fetchStockDetailFromAPI(String symbol) {
        // 토큰은 KISTokenPort를 통해 획득
        return tokenPort.getAccessToken()
                .flatMap(token -> 
                    webClient.get()
                            .uri(uriBuilder -> uriBuilder
                                    .path(kisApiProperties.stockMasterPath())
                                    .queryParam("PDNO", symbol)
                                    .build())
                            .header("Content-Type", kisApiProperties.headers().contentType())
                            .header("authorization", "Bearer " + token)
                            .header("appkey", credentialPort.getDecryptedAppKey())
                            .header("appsecret", credentialPort.getDecryptedAppSecret())
                            .header(kisApiProperties.headers().transactionId(),
                                    kisApiProperties.stockMasterTransactionId())
                            .retrieve()
                            .bodyToMono(KISStockMasterResponse.class)
                            .timeout(REQUEST_TIMEOUT)
                            .flatMap(response -> {
                                if (response.isSuccess() && response.output() != null && !response.output().isEmpty()) {
                                    Stock stock = convertToStock(response.output().get(0));
                                    return stock != null ? Mono.just(stock) : Mono.empty();
                                } else {
                                    log.error("종목 상세 조회 실패: symbol={}, code={}, msg={}", 
                                            symbol, response.returnCode(), response.message());
                                    return Mono.empty();
                                }
                            })
                )
                .doOnError(error -> log.error("종목 상세 API 호출 실패: symbol={}", symbol, error));
    }
    
    private Stock convertToStock(StockMasterOutput output) {
        try {
            String symbol = output.shortCode();
            String name = output.productName();
            String englishName = output.productEngName();
            MarketType marketType = parseMarketType(output.getMarketType());
            
            // 유효하지 않은 종목 필터링
            if (symbol == null || symbol.trim().isEmpty() || 
                name == null || name.trim().isEmpty() ||
                marketType == null) {
                return null;
            }
            
            // 종목 엔티티 생성
            if (output.isETF()) {
                return Stock.createETF(symbol, name, englishName, marketType);
            } else {
                // 섹터 정보는 추후 별도 API로 조회 필요
                return Stock.createStock(
                        symbol, 
                        name, 
                        englishName,
                        marketType, 
                        output.sectorMarketCode(),  // 섹터 코드
                        null  // 섹터명은 별도 조회 필요
                );
            }
        } catch (Exception e) {
            log.error("종목 변환 실패: shortCode={}", output.shortCode(), e);
            return null;
        }
    }
    
    private MarketType parseMarketType(String marketTypeStr) {
        if (marketTypeStr == null) return null;
        
        return switch (marketTypeStr.toUpperCase()) {
            case "KOSPI" -> MarketType.KOSPI;
            case "KOSDAQ" -> MarketType.KOSDAQ;
            case "KONEX" -> MarketType.KONEX;
            default -> {
                log.debug("알 수 없는 시장 타입: {}", marketTypeStr);
                yield null;
            }
        };
    }
    
    private String getProductTypeCode(String market) {
        return switch (market.toUpperCase()) {
            case "KOSPI" -> kisApiProperties.marketProductCodes().kospi();
            case "KOSDAQ" -> kisApiProperties.marketProductCodes().kosdaq();
            case "KONEX" -> kisApiProperties.marketProductCodes().konex();
            default -> kisApiProperties.marketProductCodes().kospi();
        };
    }
}