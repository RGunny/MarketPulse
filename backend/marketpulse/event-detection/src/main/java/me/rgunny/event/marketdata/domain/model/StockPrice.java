package me.rgunny.event.marketdata.domain.model;

import lombok.Builder;
import lombok.Getter;
import me.rgunny.event.shared.domain.value.BusinessConstants;
import me.rgunny.event.shared.domain.value.MarketDataType;
import me.rgunny.event.shared.domain.value.MarketDataValue;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@Document(collection = "stock_prices")
@CompoundIndexes({
    @CompoundIndex(name = "symbol_timestamp", def = "{'symbol': 1, 'timestamp': -1}")
})
public class StockPrice implements MarketDataValue {
    
    @Id
    private final String id;
    
    private final String symbol;              // 종목코드
    private final String name;                // 종목명
    private final BigDecimal currentPrice;    // 현재가
    private final BigDecimal previousClose;   // 전일종가
    private final BigDecimal change;          // 전일대비
    private final BigDecimal changeRate;      // 전일대비율
    private final BigDecimal high;            // 고가
    private final BigDecimal low;             // 저가
    private final BigDecimal open;            // 시가
    private final Long volume;                // 거래량
    private final BigDecimal amount;          // 거래대금
    private final BigDecimal marketCap;       // 시가총액
    
    // KIS API 추가 필드 (매도/매수 호가)
    private final BigDecimal askPrice1;       // 매도호가1
    private final BigDecimal bidPrice1;       // 매수호가1
    
    private final LocalDateTime timestamp;    // 수집시간
    
    @Indexed(expireAfter = "24h")           // 24시간 TTL
    private final LocalDateTime ttl;         // TTL (Time To Live)
    
    // MongoDB 영속성을 위한 생성자
    @PersistenceCreator
    public StockPrice(String id, String symbol, String name, BigDecimal currentPrice,
                     BigDecimal previousClose, BigDecimal change, BigDecimal changeRate,
                     BigDecimal high, BigDecimal low, BigDecimal open, Long volume,
                     BigDecimal amount, BigDecimal marketCap, BigDecimal askPrice1,
                     BigDecimal bidPrice1, LocalDateTime timestamp, LocalDateTime ttl) {
        this.id = id;
        this.symbol = symbol;
        this.name = name;
        this.currentPrice = currentPrice;
        this.previousClose = previousClose;
        this.change = change;
        this.changeRate = changeRate;
        this.high = high;
        this.low = low;
        this.open = open;
        this.volume = volume;
        this.amount = amount;
        this.marketCap = marketCap;
        this.askPrice1 = askPrice1;
        this.bidPrice1 = bidPrice1;
        this.timestamp = timestamp;
        this.ttl = ttl;
    }
    
    // MarketDataValue 인터페이스 구현
    @Override
    public String getSymbol() {
        return symbol;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public BigDecimal getCurrentValue() {
        return currentPrice;
    }
    
    @Override
    public BigDecimal getChange() {
        return change;
    }
    
    @Override
    public BigDecimal getChangeRate() {
        return changeRate;
    }
    
    @Override
    public MarketDataType getMarketDataType() {
        return MarketDataType.STOCK;
    }
    
    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    // 주식 특화 비즈니스 메서드 (불변성 유지)
    public boolean isPriceUp() {
        return isUp();
    }
    
    public boolean isPriceDown() {
        return isDown();
    }
    
    // 스프레드(매도-매수 호가 차이) 계산
    public BigDecimal getSpread() {
        if (askPrice1 == null || bidPrice1 == null) {
            return BigDecimal.ZERO;
        }
        return askPrice1.subtract(bidPrice1);
    }
    
    // 새로운 가격으로 업데이트된 인스턴스 생성 (불변성 유지)
    public StockPrice updatePrice(BigDecimal newPrice, Long newVolume, 
                                  BigDecimal newAskPrice, BigDecimal newBidPrice) {
        BigDecimal newChange = previousClose != null ? 
            newPrice.subtract(previousClose) : BusinessConstants.ZERO;
        BigDecimal newChangeRate = previousClose != null && previousClose.compareTo(BusinessConstants.ZERO) != 0 ? 
            newChange.divide(previousClose, BusinessConstants.CALCULATION_DECIMAL_PLACES, java.math.RoundingMode.HALF_UP)
                    .multiply(BusinessConstants.PERCENTAGE_MULTIPLIER) : 
            BusinessConstants.ZERO;
            
        return new StockPrice(
            this.id, this.symbol, this.name, newPrice, this.previousClose,
            newChange, newChangeRate, this.high, this.low, this.open,
            newVolume, this.amount, this.marketCap, newAskPrice, newBidPrice,
            LocalDateTime.now(), LocalDateTime.now()
        );
    }
    
    // TTL 자동 설정을 위한 팩토리 메서드
    public static StockPrice createWithTTL(String symbol, String name, BigDecimal currentPrice,
                                          BigDecimal previousClose, BigDecimal high, BigDecimal low,
                                          BigDecimal open, Long volume, BigDecimal askPrice1, BigDecimal bidPrice1) {
        LocalDateTime now = LocalDateTime.now();
        BigDecimal change = previousClose != null ? currentPrice.subtract(previousClose) : BusinessConstants.ZERO;
        BigDecimal changeRate = previousClose != null && previousClose.compareTo(BusinessConstants.ZERO) != 0 ? 
            change.divide(previousClose, BusinessConstants.CALCULATION_DECIMAL_PLACES, java.math.RoundingMode.HALF_UP)
                    .multiply(BusinessConstants.PERCENTAGE_MULTIPLIER) : 
            BusinessConstants.ZERO;
            
        return new StockPrice(
            null,  // MongoDB가 ObjectId 자동 생성
            symbol, name, currentPrice, previousClose, change, changeRate,
            high, low, open, volume, null, null,  // amount, marketCap은 null로 설정
            askPrice1, bidPrice1, now, now
        );
    }
}