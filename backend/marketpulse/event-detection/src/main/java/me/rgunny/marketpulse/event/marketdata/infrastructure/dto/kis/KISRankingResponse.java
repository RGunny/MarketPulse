package me.rgunny.marketpulse.event.marketdata.infrastructure.dto.kis;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * KIS API 순위 조회 응답 DTO
 */
@Getter
@NoArgsConstructor
@ToString
public class KISRankingResponse {
    
    @JsonProperty("rt_cd")
    private String rtCd;          // 응답코드
    
    @JsonProperty("msg_cd")
    private String msgCd;         // 메시지코드
    
    @JsonProperty("msg1")
    private String msg1;          // 메시지
    
    @JsonProperty("output")
    private List<RankingItem> output;  // 순위 데이터
    
    /**
     * 순위 항목
     */
    @Getter
    @NoArgsConstructor
    @ToString
    public static class RankingItem {
        
        @JsonProperty("hts_kor_isnm")
        private String stockName;      // 종목명
        
        @JsonProperty("mksc_shrn_iscd")
        private String symbol;         // 종목코드
        
        @JsonProperty("data_rank")
        private String rank;          // 순위
        
        @JsonProperty("stck_prpr")
        private String currentPrice;  // 현재가
        
        @JsonProperty("prdy_vrss_sign")
        private String changeSign;    // 전일 대비 부호 (1:상한, 2:상승, 3:보합, 4:하한, 5:하락)
        
        @JsonProperty("prdy_vrss")
        private String changeAmount;  // 전일 대비
        
        @JsonProperty("prdy_ctrt")
        private String changeRate;    // 전일 대비율
        
        @JsonProperty("acml_vol")
        private String volume;        // 누적 거래량
        
        @JsonProperty("prdy_vol")
        private String prevVolume;    // 전일 거래량
        
        @JsonProperty("lstn_stcn")
        private String listedShares;  // 상장 주수
        
        @JsonProperty("avrg_vol")
        private String avgVolume;     // 평균 거래량
        
        @JsonProperty("n_befr_clpr_vrss_prpr_rate")
        private String nDayChangeRate; // N일전 대비율
        
        @JsonProperty("vol_inrt")
        private String volumeRate;    // 거래량 증가율
        
        @JsonProperty("vol_tnrt")
        private String volumeTurnover; // 거래량 회전율
        
        @JsonProperty("nday_vol_tnrt")
        private String nDayVolumeTurnover; // N일 거래량 회전율
        
        @JsonProperty("avrg_tr_pbmn")
        private String avgTransactionAmount; // 평균 거래 대금
        
        @JsonProperty("tr_pbmn_tnrt")
        private String transactionTurnover; // 거래대금 회전율
        
        @JsonProperty("nday_tr_pbmn_tnrt")
        private String nDayTransactionTurnover; // N일 거래대금 회전율
        
        @JsonProperty("acml_tr_pbmn")
        private String totalTransactionAmount; // 누적 거래 대금
        
        /**
         * 상한가 여부
         */
        public boolean isLimitUp() {
            return "1".equals(changeSign);
        }
        
        /**
         * 하한가 여부
         */
        public boolean isLimitDown() {
            return "4".equals(changeSign);
        }
        
        /**
         * 상승 여부
         */
        public boolean isRising() {
            return "1".equals(changeSign) || "2".equals(changeSign);
        }
        
        /**
         * 하락 여부
         */
        public boolean isFalling() {
            return "4".equals(changeSign) || "5".equals(changeSign);
        }
    }
    
    /**
     * 응답 성공 여부
     */
    public boolean isSuccess() {
        return "0".equals(rtCd) || "00000000".equals(msgCd);
    }
}