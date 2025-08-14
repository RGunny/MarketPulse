package me.rgunny.marketpulse.event.fixture;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.rgunny.marketpulse.event.marketdata.domain.model.StockPrice;

import java.math.BigDecimal;

/**
 * StockPrice 테스트 데이터 생성을 위한 Fixture
 * Builder 패턴과 ObjectMother 패턴을 조합하여 사용
 */
public class StockPriceTestFixture {
    
    /**
     * 기본 삼성전자 주가 데이터
     */
    public static StockPrice samsung() {
        return StockPrice.createWithTTL(
                "005930", 
                "삼성전자",
                new BigDecimal("71000"),    // 현재가
                new BigDecimal("70000"),    // 전일종가
                new BigDecimal("71500"),    // 고가
                new BigDecimal("70500"),    // 저가
                new BigDecimal("70800"),    // 시가
                1000000L,                   // 거래량
                new BigDecimal("71100"),    // 매도호가1
                new BigDecimal("70900")     // 매수호가1
        );
    }
    
    /**
     * 카카오 주가 데이터
     */
    public static StockPrice kakao() {
        return StockPrice.createWithTTL(
                "035720",
                "카카오", 
                new BigDecimal("45000"),
                new BigDecimal("44000"),
                new BigDecimal("45500"),
                new BigDecimal("44500"),
                new BigDecimal("44800"),
                500000L,
                new BigDecimal("45100"),
                new BigDecimal("44900")
        );
    }
    
    /**
     * 상승주 데이터 (5% 상승)
     */
    public static StockPrice risingStock() {
        return StockPrice.createWithTTL(
                "123456",
                "상승주",
                new BigDecimal("10500"),    // 현재가
                new BigDecimal("10000"),    // 전일종가 (5% 상승)
                new BigDecimal("10600"),    // 고가
                new BigDecimal("10100"),    // 저가
                new BigDecimal("10200"),    // 시가
                800000L,                    // 거래량
                new BigDecimal("10510"),    // 매도호가1
                new BigDecimal("10490")     // 매수호가1
        );
    }
    
    /**
     * StockPrice를 JSON 문자열로 직렬화
     * 실제 ObjectMapper를 사용하여 일관성 보장
     */
    public static String toJson(StockPrice stockPrice, ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(stockPrice);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize StockPrice to JSON", e);
        }
    }
    
    /**
     * 삼성전자 JSON 문자열
     */
    public static String samsungJson(ObjectMapper objectMapper) {
        return toJson(samsung(), objectMapper);
    }
    
    /**
     * 커스텀 StockPrice 빌더
     */
    public static class Builder {
        private String symbol = "005930";
        private String name = "삼성전자";
        private BigDecimal currentPrice = new BigDecimal("71000");
        private BigDecimal previousClose = new BigDecimal("70000");
        private BigDecimal high = new BigDecimal("71500");
        private BigDecimal low = new BigDecimal("70500");
        private BigDecimal open = new BigDecimal("70800");
        private Long volume = 1000000L;
        private BigDecimal askPrice1 = new BigDecimal("71100");
        private BigDecimal bidPrice1 = new BigDecimal("70900");
        
        public Builder symbol(String symbol) {
            this.symbol = symbol;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder currentPrice(BigDecimal currentPrice) {
            this.currentPrice = currentPrice;
            return this;
        }
        
        public Builder previousClose(BigDecimal previousClose) {
            this.previousClose = previousClose;
            return this;
        }
        
        public Builder volume(Long volume) {
            this.volume = volume;
            return this;
        }
        
        public StockPrice build() {
            return StockPrice.createWithTTL(symbol, name, currentPrice, 
                    previousClose, high, low, open, volume, askPrice1, bidPrice1);
        }
        
        public String buildJson(ObjectMapper objectMapper) {
            return toJson(build(), objectMapper);
        }
    }
    
    /**
     * 새로운 Builder 인스턴스 생성
     */
    public static Builder aStockPrice() {
        return new Builder();
    }
}