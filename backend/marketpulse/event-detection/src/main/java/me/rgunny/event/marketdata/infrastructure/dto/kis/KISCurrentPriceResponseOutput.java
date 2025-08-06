package me.rgunny.event.marketdata.infrastructure.dto.kis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KISCurrentPriceResponseOutput(
        String stck_prpr,      // 주식 현재가
        String prdy_vrss,      // 전일 대비
        String prdy_vrss_sign, // 전일 대비 부호
        String prdy_ctrt,      // 전일 대비율
        String stck_prdy_clpr, // 주식 전일 종가
        String acml_vol,       // 누적 거래량
        String acml_tr_pbmn,   // 누적 거래 대금
        String seln_cntg_qty,  // 매도 체결 수량
        String shnu_cntg_qty,  // 매수 체결 수량
        String ntby_cntg_qty,  // 순매수 체결 수량
        String cttr,           // 체결강도
        String seln_cntg_smtn, // 총 매도수량
        String shnu_cntg_smtn, // 총 매수수량
        String ccld_dvsn,      // 체결구분
        String shnu_rate,      // 매수비율
        String prdy_vol_vrss_acml_vol_rate, // 전일 거래량 대비 등락율
        String oprc_hour,      // 시가 시간
        String oprc_vrss_prpr_sign, // 시가대비구분
        String oprc_vrss_prpr, // 시가 대비
        String hgpr_hour,      // 최고가 시간
        String hgpr_vrss_prpr_sign, // 고가대비구분
        String hgpr_vrss_prpr, // 고가 대비
        String lwpr_hour,      // 최저가 시간
        String lwpr_vrss_prpr_sign, // 저가대비구분
        String lwpr_vrss_prpr, // 저가 대비
        String bsop_date,      // 영업 일자
        String new_mkop_cls_code, // 신 시장 분류 코드
        String trht_yn,        // 거래정지 여부
        String askp1,          // 매도호가1
        String askp2,          // 매도호가2
        String askp3,          // 매도호가3
        String askp4,          // 매도호가4
        String askp5,          // 매도호가5
        String bidp1,          // 매수호가1
        String bidp2,          // 매수호가2
        String bidp3,          // 매수호가3
        String bidp4,          // 매수호가4
        String bidp5,          // 매수호가5
        String askp_rsqn1,     // 매도호가 잔량1
        String askp_rsqn2,     // 매도호가 잔량2
        String askp_rsqn3,     // 매도호가 잔량3
        String askp_rsqn4,     // 매도호가 잔량4
        String askp_rsqn5,     // 매도호가 잔량5
        String bidp_rsqn1,     // 매수호가 잔량1
        String bidp_rsqn2,     // 매수호가 잔량2
        String bidp_rsqn3,     // 매수호가 잔량3
        String bidp_rsqn4,     // 매수호가 잔량4
        String bidp_rsqn5,     // 매수호가 잔량5
        String stck_hgpr,      // 주식 최고가
        String stck_lwpr,      // 주식 최저가
        String stck_oprc,      // 주식 시가
        String stck_mxpr,      // 주식 상한가
        String stck_llam,      // 주식 하한가
        String stck_sdpr,      // 주식 기준가
        String wghn_avrg_stck_prc, // 가중 평균 주식 가격
        String hts_frgn_ehrt,  // HTS 외국인 소진율
        String frgn_ntby_qty,  // 외국인 순매수 수량
        String pgtr_ntby_qty,  // 프로그램매매 순매수 수량
        String pvt_scnd_dmrs_prc, // 피벗 2차 디저항 가격
        String pvt_frst_dmrs_prc, // 피벗 1차 디저항 가격
        String pvt_pont_val,   // 피벗 포인트 값
        String pvt_frst_dmsp_prc, // 피벗 1차 디지지 가격
        String pvt_scnd_dmsp_prc, // 피벗 2차 디지지 가격
        String dmrs_val,       // 디저항 값
        String dmsp_val,       // 디지지 값
        String cpfn,           // 자본금
        String rstc_wdth_prc,  // 제한폭 가격
        String stck_fcam,      // 주식 액면가
        String stck_sspr,      // 주식 대용가
        String aspr_unit,      // 호가단위
        String hts_deal_qty_unit_val, // HTS 매매 수량 단위 값
        String lst_stck_qntq,  // 상장 주식 수량
        String hts_avls,       // HTS 시가총액
        String per,            // PER
        String pbr,            // PBR
        String stac_month,     // 결산 월
        String vol_tnrt,       // 거래량 회전율
        String eps,            // EPS
        String bps,            // BPS
        String d250_hgpr,      // 250일 최고가
        String d250_hgpr_date, // 250일 최고가 일자
        String d250_hgpr_vrss_prpr_rate, // 250일 최고가 대비 현재가 비율
        String d250_lwpr,      // 250일 최저가
        String d250_lwpr_date, // 250일 최저가 일자
        String d250_lwpr_vrss_prpr_rate, // 250일 최저가 대비 현재가 비율
        String stck_dryy_hgpr, // 주식 연중 최고가
        String dryy_hgpr_vrss_prpr_rate, // 연중 최고가 대비 현재가 비율
        String dryy_hgpr_date, // 연중 최고가 일자
        String stck_dryy_lwpr, // 주식 연중 최저가
        String dryy_lwpr_vrss_prpr_rate, // 연중 최저가 대비 현재가 비율
        String dryy_lwpr_date, // 연중 최저가 일자
        String w52_hgpr,       // 52주일 최고가
        String w52_hgpr_vrss_prpr_ctrt, // 52주일 최고가 대비 등락율
        String w52_hgpr_date,  // 52주일 최고가 일자
        String w52_lwpr,       // 52주일 최저가
        String w52_lwpr_vrss_prpr_ctrt, // 52주일 최저가 대비 등락율
        String w52_lwpr_date,  // 52주일 최저가 일자
        String whol_loan_rmnd_rate, // 전체 융자 잔고 비율
        String ssts_yn,        // 공매도과열종목여부
        String stck_shrn_iscd, // 주식 단축 종목코드
        String fcam_cnnm,      // 액면가 통화명
        String cpfn_cnnm,      // 자본금 통화명
        String frgn_hldn_qty,  // 외국인 보유 수량
        String vi_cls_code,    // VI적용구분코드
        String ovtm_vi_cls_code, // 시간외단일가VI적용구분코드
        String last_ssts_cntg_qty, // 최종 공매도 체결 수량
        String invt_caful_yn,  // 투자유의종목여부
        String mrkt_warn_cls_code, // 시장경고코드
        String short_over_yn,  // 단기과열여부
        String sltr_yn         // 정리매매종목여부
) {}