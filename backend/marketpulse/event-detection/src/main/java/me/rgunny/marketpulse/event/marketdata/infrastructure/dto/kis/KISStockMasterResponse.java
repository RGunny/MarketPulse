package me.rgunny.marketpulse.event.marketdata.infrastructure.dto.kis;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * KIS API 종목 마스터 조회 응답
 * 전종목 기본정보 조회 API 응답 구조
 */
public record KISStockMasterResponse(
        @JsonProperty("rt_cd") String returnCode,           // 성공/실패 구분 코드
        @JsonProperty("msg_cd") String messageCode,         // 응답 메시지 코드
        @JsonProperty("msg1") String message,               // 응답 메시지
        @JsonProperty("output") List<StockMasterOutput> output  // 종목 마스터 목록
) {
    
    /**
     * 성공 여부 확인
     */
    public boolean isSuccess() {
        return "0".equals(returnCode);
    }
    
    /**
     * 종목 마스터 상세 정보
     */
    public record StockMasterOutput(
            @JsonProperty("isin_cd") String isinCode,           // ISIN 코드
            @JsonProperty("isin_nm") String isinName,           // ISIN 명
            @JsonProperty("std_pdno") String standardCode,      // 표준 상품번호
            @JsonProperty("shrt_pdno") String shortCode,        // 단축 상품번호 (종목코드)
            @JsonProperty("prdt_name") String productName,      // 상품명 (종목명)
            @JsonProperty("prdt_eng_name") String productEngName, // 영문 상품명
            @JsonProperty("prdt_type_cd") String productTypeCode, // 상품 유형 코드
            @JsonProperty("prdt_clsf_cd") String productClassCode, // 상품 분류 코드
            @JsonProperty("lstg_stqt") String listedQuantity,   // 상장 주수
            @JsonProperty("lstg_cptl_amt") String listedCapital, // 상장 자본금
            @JsonProperty("cptl_amt") String capital,           // 자본금
            @JsonProperty("prdt_abrv") String productAbbrv,     // 상품 약어
            @JsonProperty("etf_yn") String etfYn,               // ETF 여부
            @JsonProperty("spac_yn") String spacYn,             // SPAC 여부
            @JsonProperty("kospi200_yn") String kospi200Yn,     // KOSPI200 구성 여부
            @JsonProperty("kosdaq150_yn") String kosdaq150Yn,   // KOSDAQ150 구성 여부
            @JsonProperty("krx100_yn") String krx100Yn,         // KRX100 구성 여부
            @JsonProperty("kospi_yn") String kospiYn,           // KOSPI 종목 여부
            @JsonProperty("kosdaq_yn") String kosdaqYn,         // KOSDAQ 종목 여부
            @JsonProperty("konex_yn") String konexYn,           // KONEX 종목 여부
            @JsonProperty("scts_mket_cd") String sectorMarketCode, // 섹터 시장 코드
            @JsonProperty("fcam_mod_cls_cd") String fcamModCode, // FCAM 분류 코드
            @JsonProperty("icic_cls_cd") String icicClassCode,  // ICIC 분류 코드
            @JsonProperty("stck_kind_cd") String stockKindCode, // 주식 종류 코드
            @JsonProperty("mktc_shrn_iscd") String marketShareCode, // 시장 구분 코드
            @JsonProperty("lstg_abolition_dt") String delistDate, // 상장폐지일
            @JsonProperty("lstg_dt") String listDate,           // 상장일
            @JsonProperty("thdt_rsfl_rate") String todayFluctRate, // 당일 등락률
            @JsonProperty("thdt_rsfl_amt") String todayFluctAmt, // 당일 등락 금액
            @JsonProperty("stck_prpr") String currentPrice,     // 현재가
            @JsonProperty("stck_oprc") String openPrice,        // 시가
            @JsonProperty("stck_hgpr") String highPrice,        // 고가
            @JsonProperty("stck_lwpr") String lowPrice,         // 저가
            @JsonProperty("acml_vol") String accVolume,         // 누적 거래량
            @JsonProperty("acml_tr_pbmn") String accTrAmount,   // 누적 거래대금
            @JsonProperty("hts_avls") String htsAvailable      // HTS 가능 여부
    ) {
        
        /**
         * 거래 가능 여부 확인
         */
        public boolean isTradable() {
            return "Y".equals(htsAvailable) && 
                   (delistDate == null || delistDate.isEmpty() || "00000000".equals(delistDate));
        }
        
        /**
         * ETF 여부 확인
         */
        public boolean isETF() {
            return "Y".equals(etfYn);
        }
        
        /**
         * SPAC 여부 확인
         */
        public boolean isSPAC() {
            return "Y".equals(spacYn);
        }
        
        /**
         * 주요 지수 구성 종목 여부
         */
        public boolean isMajorIndex() {
            return "Y".equals(kospi200Yn) || "Y".equals(kosdaq150Yn) || "Y".equals(krx100Yn);
        }
        
        /**
         * 시장 구분 확인
         */
        public String getMarketType() {
            if ("Y".equals(kospiYn)) return "KOSPI";
            if ("Y".equals(kosdaqYn)) return "KOSDAQ";
            if ("Y".equals(konexYn)) return "KONEX";
            return "UNKNOWN";
        }
    }
}