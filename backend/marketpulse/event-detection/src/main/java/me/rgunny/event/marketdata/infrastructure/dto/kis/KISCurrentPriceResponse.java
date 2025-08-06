package me.rgunny.event.marketdata.infrastructure.dto.kis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KISCurrentPriceResponse(
        String rt_cd,        // 성공실패 구분코드 (0: 성공)
        String msg_cd,       // 응답코드
        String msg1,         // 응답메시지
        KISCurrentPriceResponseOutput output        // 응답 상세
) {}