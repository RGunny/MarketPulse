package me.rgunny.marketpulse.event.marketdata.domain.model;

import lombok.Builder;
import lombok.Getter;
import me.rgunny.marketpulse.event.marketdata.domain.error.StockErrorCode;
import me.rgunny.marketpulse.common.core.util.Validator;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 종목 정보 도메인 엔티티
 */
@Getter
@Builder
@Document(collection = "stock")
@CompoundIndexes({
    @CompoundIndex(name = "market_symbol", def = "{'marketType': 1, 'symbol': 1}", unique = true)
})
public class Stock {
    
    @Id
    private final String id;
    
    @Indexed(unique = true)
    private final String symbol;           // 종목코드 (예: 005930)
    private final String name;             // 종목명 (예: 삼성전자)
    private final String englishName;      // 영문 종목명 (예: Samsung Electronics)
    private final MarketType marketType;   // 시장구분 (KOSPI, KOSDAQ, KONEX)
    private final String sectorCode;       // 업종코드
    private final String sectorName;       // 업종명 (예: 전기전자)
    private final boolean isActive;        // 거래정지 여부 (true: 정상, false: 거래정지)
    private final boolean isETF;           // ETF 여부
    private final boolean isSPAC;          // SPAC 여부
    
    private final LocalDateTime listedDate;   // 상장일
    private final LocalDateTime delistedDate; // 상장폐지일 (null이면 거래중)
    
    private final LocalDateTime createdAt;    // 생성일시
    private final LocalDateTime updatedAt;    // 수정일시

    // MongoDB 영속성을 위한 생성자
    @PersistenceCreator
    public Stock(String id, String symbol, String name, String englishName,
                 MarketType marketType, String sectorCode, String sectorName,
                 boolean isActive, boolean isETF, boolean isSPAC,
                 LocalDateTime listedDate, LocalDateTime delistedDate,
                 LocalDateTime createdAt, LocalDateTime updatedAt) {

        validateForCreate(symbol, name, marketType);

        this.id = id;
        this.symbol = symbol;
        this.name = name;
        this.englishName = englishName;
        this.marketType = marketType;
        this.sectorCode = sectorCode;
        this.sectorName = sectorName;
        this.isActive = isActive;
        this.isETF = isETF;
        this.isSPAC = isSPAC;
        this.listedDate = listedDate;
        this.delistedDate = delistedDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    private void validateForCreate(String symbol, String name, MarketType marketType) {
        Validator.requireNotBlank(symbol, StockErrorCode.STOCK_SYMBOL_REQUIRED);
        Validator.requireNotBlank(name, StockErrorCode.STOCK_NAME_REQUIRED);
        Validator.requireNotNull(marketType, StockErrorCode.STOCK_MARKET_TYPE_REQUIRED);
        Validator.requirePattern(symbol, "\\d{6}", StockErrorCode.STOCK_INVALID_SYMBOL_FORMAT);
    }

    // 비즈니스 메서드
    
    /**
     * 거래 가능 여부 확인
     */
    public boolean isTradable() {
        return isActive && delistedDate == null;
    }
    
    /**
     * 주요 종목 여부 판단
     * 현재는 KOSPI/KOSDAQ 시장 상장 종목을 주요 종목으로 분류
     * 향후 KOSPI200, KOSDAQ150 지수 구성 종목 정보가 추가되면 정밀한 판단 가능
     */
    public boolean isMajorStock() {
        return marketType == MarketType.KOSPI || marketType == MarketType.KOSDAQ;
    }
    
    /**
     * 종목 정보 업데이트
     */
    public Stock updateInfo(String name, String sectorCode, String sectorName, boolean isActive) {
        return new Stock(
            this.id, this.symbol, name, this.englishName,
            this.marketType, sectorCode, sectorName,
            isActive, this.isETF, this.isSPAC,
            this.listedDate, this.delistedDate,
            this.createdAt, LocalDateTime.now()
        );
    }
    
    /**
     * 상장폐지 처리
     */
    public Stock delist() {
        return new Stock(
            this.id, this.symbol, this.name, this.englishName,
            this.marketType, this.sectorCode, this.sectorName,
            false, this.isETF, this.isSPAC,
            this.listedDate, LocalDateTime.now(),
            this.createdAt, LocalDateTime.now()
        );
    }
    
    // 팩토리 메서드
    
    /**
     * 일반 주식 생성
     */
    public static Stock createStock(String symbol, String name, String englishName,
                                    MarketType marketType, String sectorCode, String sectorName) {
        LocalDateTime now = LocalDateTime.now();
        return new Stock(
            null,  // MongoDB가 ObjectId 자동 생성
            symbol, name, englishName,
            marketType, sectorCode, sectorName,
            true, false, false,  // 활성, ETF 아님, SPAC 아님
            now, null,  // 상장일은 현재, 상장폐지일은 null
            now, now
        );
    }
    
    /**
     * ETF 생성
     */
    public static Stock createETF(String symbol, String name, String englishName, MarketType marketType) {
        LocalDateTime now = LocalDateTime.now();
        return new Stock(
            null,  // MongoDB가 ObjectId 자동 생성
            symbol, name, englishName,
            marketType, "ETF", "ETF",
            true, true, false,  // 활성, ETF임, SPAC 아님
            now, null,
            now, now
        );
    }

}