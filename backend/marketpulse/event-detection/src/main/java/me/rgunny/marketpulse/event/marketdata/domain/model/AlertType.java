package me.rgunny.marketpulse.event.marketdata.domain.model;

import lombok.Getter;

/**
 * 알림 타입 정의
 * 각 타입별로 독립적인 쿨다운 관리
 */
@Getter
public enum AlertType {
    PRICE_RISE("급등"),
    PRICE_FALL("급락"),
    LIMIT_UP("상한가"),
    LIMIT_DOWN("하한가"),
    VOLUME_SURGE("거래량 급증"),
    NEWS_ALERT("뉴스 알림")
    ;

    private final String description;

    AlertType(String description) {
        this.description = description;
    }

}

