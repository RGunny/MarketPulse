package me.rgunny.marketpulse.event.marketdata.infrastructure.adapter.out.kis;

import lombok.extern.slf4j.Slf4j;
import me.rgunny.marketpulse.event.marketdata.domain.model.MarketRanking;
import me.rgunny.marketpulse.event.marketdata.domain.model.MarketType;
import me.rgunny.marketpulse.event.marketdata.infrastructure.config.kis.KISApiProperties;
import me.rgunny.marketpulse.event.marketdata.infrastructure.dto.kis.KISRankingResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.Duration;

/**
 * KIS API 시장 순위 조회 어댑터
 */
@Slf4j
@Component
public class KISMarketRankingAdapter {
    
    private final WebClient kisWebClient;
    private final KISTokenService tokenService;
    private final KISApiProperties apiProperties;
    
    public KISMarketRankingAdapter(
            @Qualifier("kisWebClient") WebClient kisWebClient,
            KISTokenService tokenService,
            KISApiProperties apiProperties) {
        this.kisWebClient = kisWebClient;
        this.tokenService = tokenService;
        this.apiProperties = apiProperties;
    }
    
    private static final String RANKING_URI = "/uapi/domestic-stock/v1/ranking/fluctuation";
    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    
    /**
     * 상승률 상위 종목 조회
     */
    public Flux<MarketRanking> fetchTopGainers(MarketType marketType, int limit) {
        log.debug("Fetching top gainers: market={}, limit={}", marketType, limit);
        return fetchRankings(marketType, "0", "0", limit)
                .map(item -> convertToMarketRanking(
                        item, marketType, MarketRanking.RankingType.PRICE_RISE));
    }
    
    /**
     * 하락률 상위 종목 조회
     */
    public Flux<MarketRanking> fetchTopLosers(MarketType marketType, int limit) {
        log.debug("Fetching top losers: market={}, limit={}", marketType, limit);
        return fetchRankings(marketType, "0", "1", limit)
                .map(item -> convertToMarketRanking(
                        item, marketType, MarketRanking.RankingType.PRICE_FALL));
    }
    
    /**
     * 거래량 급증 종목 조회
     */
    public Flux<MarketRanking> fetchVolumeLeaders(MarketType marketType, int limit) {
        log.debug("Fetching volume leaders: market={}, limit={}", marketType, limit);
        return fetchRankings(marketType, "1", "0", limit)
                .map(item -> convertToMarketRanking(
                        item, marketType, MarketRanking.RankingType.VOLUME_SURGE));
    }
    
    /**
     * KIS API 순위 조회 공통 메서드
     * 
     * @param marketType 시장 구분
     * @param fid_rank_sort_cls_code 순위 정렬 구분 (0: 등락률, 1: 거래량)
     * @param fid_prc_cls_code 가격 구분 (0: 상승, 1: 하락)
     * @param limit 조회 건수
     */
    private Flux<KISRankingResponse.RankingItem> fetchRankings(
            MarketType marketType, String sortCode, String priceCode, int limit) {
        
        return tokenService.getAccessToken()
                .flatMapMany(token -> kisWebClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path(RANKING_URI)
                                .queryParams(buildRankingQueryParams(marketType, sortCode, priceCode))
                                .build())
                        .headers(headers -> buildRankingHeaders(headers, token, sortCode, priceCode))
                        .retrieve()
                        .bodyToMono(KISRankingResponse.class)
                        .timeout(TIMEOUT)
                        .doOnNext(response -> logResponseStatus(response, marketType, sortCode, priceCode))
                        .filter(KISRankingResponse::isSuccess)
                        .flatMapMany(response -> Flux.fromIterable(response.getOutput()))
                        .take(limit))
                .doOnError(error -> log.error("Failed to fetch rankings for market: {}, error: {}", 
                        marketType, error.getMessage()))
                .onErrorResume(error -> {
                    log.warn("Ranking fetch failed for market: {}, returning empty. Error: {}", 
                            marketType, error.getMessage());
                    return Flux.empty();
                });
    }
    
    /**
     * 순위 조회 Query Parameters 생성
     */
    private org.springframework.util.MultiValueMap<String, String> buildRankingQueryParams(
            MarketType marketType, String sortCode, String priceCode) {
        
        var params = new org.springframework.util.LinkedMultiValueMap<String, String>();
        params.add("fid_rank_sort_cls_code", sortCode);
        params.add("fid_prc_cls_code", priceCode);
        params.add("fid_cond_mrkt_div_code", "J");      // KIS API는 시장 구분 없이 "J"(주식 전체)만 지원
        params.add("fid_cond_scr_div_code", "20174");   // 국내주식 고정
        params.add("fid_input_iscd", "0000");           // 전체종목
        params.add("fid_div_cls_code", "0");            // 일반
        params.add("fid_input_price_1", "");            // 가격 하한 (미사용)
        params.add("fid_input_price_2", "");            // 가격 상한 (미사용)
        params.add("fid_vol_cnt", "");                  // 거래량 조건 (미사용)
        params.add("fid_input_date_1", "");             // 날짜 조건 (미사용)
        
        return params;
    }
    
    /**
     * 순위 조회 Headers 설정
     */
    private void buildRankingHeaders(
            org.springframework.http.HttpHeaders headers, 
            String token, String sortCode, String priceCode) {
        
        headers.setBearerAuth(token);
        headers.set("appkey", apiProperties.appKey());
        headers.set("appsecret", apiProperties.appSecret());
        headers.set("tr_id", getRankingTransactionId(sortCode, priceCode));
        headers.set("custtype", apiProperties.headers().personalCustomerType());
    }
    
    /**
     * 응답 상태 로깅
     */
    private void logResponseStatus(
            KISRankingResponse response, 
            MarketType marketType, String sortCode, String priceCode) {
        
        if (!response.isSuccess()) {
            log.error("KIS API ranking error - market: {}, sort: {}, price: {}, rtCd: {}, msg: {}", 
                    marketType, sortCode, priceCode, response.getRtCd(), response.getMsg1());
        } else {
            log.debug("KIS API ranking success - market: {}, sort: {}, price: {}, count: {}",
                    marketType, sortCode, priceCode, 
                    response.getOutput() != null ? response.getOutput().size() : 0);
        }
    }
    
    /**
     * 순위 조회용 트랜잭션 ID 생성
     * KIS API 문서에 따른 올바른 tr_id 반환
     */
    private String getRankingTransactionId(String sortCode, String priceCode) {

        if ("0".equals(sortCode)) return "FHPST01740000"; // 등락률 순위
        else if ("1".equals(sortCode)) return "FHPST01750000"; // 거래량 순위

        return "FHPST01740000"; // 기본값
    }
    
    /**
     * DTO를 도메인 모델로 변환
     */
    private MarketRanking convertToMarketRanking(
            KISRankingResponse.RankingItem item,
            MarketType marketType,
            MarketRanking.RankingType rankingType) {
        
        // 상한가/하한가 체크
        if (item.isLimitUp()) {
            rankingType = MarketRanking.RankingType.LIMIT_UP;
        } else if (item.isLimitDown()) {
            rankingType = MarketRanking.RankingType.LIMIT_DOWN;
        }
        
        return MarketRanking.create(
                item.getSymbol(),
                item.getStockName(),
                marketType,
                rankingType,
                Integer.parseInt(item.getRank()),
                new BigDecimal(item.getCurrentPrice()),
                new BigDecimal(item.getChangeRate()),
                new BigDecimal(item.getChangeAmount()),
                Long.parseLong(item.getVolume()),
                item.getVolumeRate() != null ? new BigDecimal(item.getVolumeRate()) : null
        );
    }
}