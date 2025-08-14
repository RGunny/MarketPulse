package me.rgunny.marketpulse.event.shared.domain.value;

/**
 * 시장 데이터 타입
 * 주식, 환율, 원자재 등 다양한 시장 데이터를 구분
 */
public enum MarketDataType {
    
    STOCK("주식", "국내외 주식 시세"),
    INDEX("지수", "주가지수, 업종지수"),
    CURRENCY("환율", "통화 환율 정보"),
    COMMODITY("원자재", "원유, 금, 구리 등 원자재 가격"),
    INTEREST_RATE("금리", "기준금리, 시장금리"),
    CRYPTO("암호화폐", "비트코인, 이더리움 등");
    
    private final String displayName;
    private final String description;
    
    MarketDataType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}