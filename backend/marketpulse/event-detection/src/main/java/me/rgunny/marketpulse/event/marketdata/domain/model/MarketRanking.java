package me.rgunny.marketpulse.event.marketdata.domain.model;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 시장 순위 데이터 도메인 엔티티
 * 
 * 실시간 시장에서 급등/급락/거래량 상위 종목을 저장
 */
@Getter
@Builder
@Document(collection = "market_rankings")
@CompoundIndexes({
    @CompoundIndex(name = "type_rank_time", def = "{'rankingType': 1, 'rank': 1, 'capturedAt': -1}"),
    @CompoundIndex(name = "symbol_type_time", def = "{'symbol': 1, 'rankingType': 1, 'capturedAt': -1}")
})
public class MarketRanking {
    
    @Id
    private final String id;
    
    private final String symbol;              // 종목코드
    private final String name;                // 종목명
    private final MarketType marketType;      // 시장구분 (KOSPI, KOSDAQ)
    private final RankingType rankingType;    // 순위 유형
    private final int rank;                   // 순위 (1~30)
    
    private final BigDecimal currentPrice;    // 현재가
    private final BigDecimal changeRate;      // 등락률 (%)
    private final BigDecimal changeAmount;    // 등락폭
    private final Long volume;                // 거래량
    private final BigDecimal volumeRate;      // 거래량 증가율 (%)
    
    private final LocalDateTime capturedAt;   // 수집 시간
    private final LocalDateTime createdAt;    // 생성 시간
    
    /**
     * 순위 유형
     */
    public enum RankingType {
        PRICE_RISE("상승률 상위"),
        PRICE_FALL("하락률 상위"),
        VOLUME_SURGE("거래량 급증"),
        LIMIT_UP("상한가"),
        LIMIT_DOWN("하한가"),
        NEW_HIGH("52주 신고가"),
        NEW_LOW("52주 신저가");
        
        private final String description;
        
        RankingType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 급등 종목 여부 (상승률 5% 이상)
     */
    public boolean isSurging() {
        return rankingType == RankingType.PRICE_RISE && 
               changeRate != null && 
               changeRate.compareTo(BigDecimal.valueOf(5)) >= 0;
    }
    
    /**
     * 급락 종목 여부 (하락률 -5% 이하)
     */
    public boolean isPlunging() {
        return rankingType == RankingType.PRICE_FALL && 
               changeRate != null && 
               changeRate.compareTo(BigDecimal.valueOf(-5)) <= 0;
    }
    
    /**
     * 거래량 급증 여부 (평균 대비 300% 이상)
     */
    public boolean isVolumeSurging() {
        return rankingType == RankingType.VOLUME_SURGE && 
               volumeRate != null && 
               volumeRate.compareTo(BigDecimal.valueOf(300)) >= 0;
    }
    
    /**
     * WatchTarget 자동 등록 대상 여부 (기본 규칙)
     * - 상승률 상위 10위 이내
     * - 하락률 상위 10위 이내
     * - 거래량 급증 상위 5위 이내
     * - 상한가/하한가 종목
     */
    public boolean shouldAutoWatch() {
        return shouldAutoWatch(10, 10, 5);
    }
    
    /**
     * WatchTarget 자동 등록 대상 여부 (설정 가능)
     * @param priceRiseLimit 상승률 순위 제한
     * @param priceFallLimit 하락률 순위 제한
     * @param volumeSurgeLimit 거래량 순위 제한
     */
    public boolean shouldAutoWatch(int priceRiseLimit, int priceFallLimit, int volumeSurgeLimit) {
        return switch (rankingType) {
            case PRICE_RISE -> rank <= priceRiseLimit;
            case PRICE_FALL -> rank <= priceFallLimit;
            case VOLUME_SURGE -> rank <= volumeSurgeLimit;
            case LIMIT_UP, LIMIT_DOWN -> true;
            case NEW_HIGH, NEW_LOW -> rank <= 3;
        };
    }
    
    /**
     * 팩토리 메서드: 순위 데이터 생성
     */
    public static MarketRanking create(
            String symbol, String name, MarketType marketType,
            RankingType rankingType, int rank,
            BigDecimal currentPrice, BigDecimal changeRate,
            BigDecimal changeAmount, Long volume, BigDecimal volumeRate) {
        
        LocalDateTime now = LocalDateTime.now();
        return MarketRanking.builder()
                .symbol(symbol)
                .name(name)
                .marketType(marketType)
                .rankingType(rankingType)
                .rank(rank)
                .currentPrice(currentPrice)
                .changeRate(changeRate)
                .changeAmount(changeAmount)
                .volume(volume)
                .volumeRate(volumeRate)
                .capturedAt(now)
                .createdAt(now)
                .build();
    }
}