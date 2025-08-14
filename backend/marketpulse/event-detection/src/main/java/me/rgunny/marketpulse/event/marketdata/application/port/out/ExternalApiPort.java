package me.rgunny.marketpulse.event.marketdata.application.port.out;

import me.rgunny.marketpulse.event.shared.domain.value.MarketDataType;
import me.rgunny.marketpulse.event.shared.domain.value.MarketDataValue;
import reactor.core.publisher.Mono;

/**
 * 외부 API 통합을 위한 범용 포트
 * Strategy 패턴으로 다양한 외부 API 지원
 */
public interface ExternalApiPort {
    
    /**
     * API가 지원하는 데이터 타입인지 확인
     * @param dataType 요청할 데이터 타입
     * @return 지원 여부
     */
    boolean supports(MarketDataType dataType);

    /**
     * 시장 데이터 조회 (범용)
     * @param symbol 종목/심볼 코드
     * @param dataType 요청할 데이터 타입
     * @return 시장 데이터
     */
    <T extends MarketDataValue> Mono<T> fetchMarketData(String symbol, MarketDataType dataType, Class<T> clazz);
    
    /**
     * API 제공자 이름
     * @return API 제공자 (예: "KIS", "AlphaVantage", "Yahoo")
     */
    String getProviderName();
    
    /**
     * API 요청 제한 정보
     * @return 분당 최대 요청 수
     */
    int getRateLimitPerMinute();
}