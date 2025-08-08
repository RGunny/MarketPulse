package me.rgunny.event.marketdata.domain.model;

public enum MarketType {

    KOSPI("코스피"),
    KOSDAQ("코스닥"),
    KONEX("코넥스"),
    NASDAQ("나스닥"),  // 향후 해외 주식 지원 대비
    NYSE("뉴욕증권거래소")
    ;

    private final String description;

    MarketType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

}
