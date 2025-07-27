package me.rgunny.event.unit.domain.stock;

import me.rgunny.event.domain.market.MarketDataType;
import me.rgunny.event.domain.stock.StockPrice;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("StockPrice 도메인 엔티티 - 불변 객체 (unit)")
class StockPriceTest {

    @Test
    @DisplayName("StockPrice 생성 시 모든 필드가 final로 불변성을 유지한다")
    void givenStockPriceData_whenCreateStockPrice_thenAllFieldsAreImmutable() {
        // given
        String symbol = "005930";
        String name = "삼성전자";
        BigDecimal currentPrice = new BigDecimal("70000");
        BigDecimal previousClose = new BigDecimal("71000");
        BigDecimal high = new BigDecimal("71500");
        BigDecimal low = new BigDecimal("69800");
        BigDecimal open = new BigDecimal("70500");
        Long volume = 15000000L;
        BigDecimal askPrice1 = new BigDecimal("70100");
        BigDecimal bidPrice1 = new BigDecimal("69900");

        // when
        StockPrice stockPrice = StockPrice.createWithTTL(
            symbol, name, currentPrice, previousClose, high, low, open, volume, askPrice1, bidPrice1
        );

        // then
        assertThat(stockPrice.getSymbol()).isEqualTo(symbol);
        assertThat(stockPrice.getName()).isEqualTo(name);
        assertThat(stockPrice.getCurrentPrice()).isEqualByComparingTo(currentPrice);
        assertThat(stockPrice.getPreviousClose()).isEqualByComparingTo(previousClose);
        assertThat(stockPrice.getMarketDataType()).isEqualTo(MarketDataType.STOCK);
        assertThat(stockPrice.getTimestamp()).isNotNull();
        assertThat(stockPrice.getTtl()).isNotNull();
    }

    @Test
    @DisplayName("전일 대비 상승 시 change와 changeRate가 양수로 계산된다")
    void givenPriceUp_whenCreateStockPrice_thenChangeAndRateArePositive() {
        // given
        BigDecimal currentPrice = new BigDecimal("71000");
        BigDecimal previousClose = new BigDecimal("70000");

        // when
        StockPrice stockPrice = StockPrice.createWithTTL(
            "005930", "삼성전자", currentPrice, previousClose, 
            currentPrice, previousClose, currentPrice, 1000000L,
            new BigDecimal("71100"), new BigDecimal("70900")
        );

        // then
        assertThat(stockPrice.getChange()).isEqualByComparingTo(new BigDecimal("1000"));
        assertThat(stockPrice.getChangeRate()).isPositive();
        assertThat(stockPrice.isPriceUp()).isTrue();
        assertThat(stockPrice.isPriceDown()).isFalse();
        assertThat(stockPrice.isUp()).isTrue();
    }

    @Test
    @DisplayName("전일 대비 하락 시 change와 changeRate가 음수로 계산된다")
    void givenPriceDown_whenCreateStockPrice_thenChangeAndRateAreNegative() {
        // given
        BigDecimal currentPrice = new BigDecimal("69000");
        BigDecimal previousClose = new BigDecimal("70000");

        // when
        StockPrice stockPrice = StockPrice.createWithTTL(
            "005930", "삼성전자", currentPrice, previousClose,
            previousClose, currentPrice, currentPrice, 1000000L,
            new BigDecimal("69100"), new BigDecimal("68900")
        );

        // then
        assertThat(stockPrice.getChange()).isEqualByComparingTo(new BigDecimal("-1000"));
        assertThat(stockPrice.getChangeRate()).isNegative();
        assertThat(stockPrice.isPriceUp()).isFalse();
        assertThat(stockPrice.isPriceDown()).isTrue();
        assertThat(stockPrice.isDown()).isTrue();
    }

    @Test
    @DisplayName("5% 이상 변동 시 유의미한 변화로 판단한다")
    void givenSignificantPriceChange_whenCheckSignificantChange_thenReturnsTrue() {
        // given
        BigDecimal currentPrice = new BigDecimal("75000");
        BigDecimal previousClose = new BigDecimal("70000");
        BigDecimal threshold = new BigDecimal("5.0");

        StockPrice stockPrice = StockPrice.createWithTTL(
            "005930", "삼성전자", currentPrice, previousClose,
            currentPrice, previousClose, currentPrice, 1000000L,
            new BigDecimal("75100"), new BigDecimal("74900")
        );

        // when
        boolean isSignificant = stockPrice.isSignificantChange(threshold);

        // then
        assertThat(isSignificant).isTrue();
        assertThat(stockPrice.getChangeRate().abs()).isGreaterThan(threshold);
    }

    @Test
    @DisplayName("매도호가와 매수호가 차이로 스프레드를 계산한다")
    void givenAskAndBidPrices_whenCalculateSpread_thenReturnsCorrectSpread() {
        // given
        BigDecimal askPrice1 = new BigDecimal("70100");
        BigDecimal bidPrice1 = new BigDecimal("69900");
        BigDecimal expectedSpread = new BigDecimal("200");

        StockPrice stockPrice = StockPrice.createWithTTL(
            "005930", "삼성전자", new BigDecimal("70000"), new BigDecimal("70000"),
            new BigDecimal("70000"), new BigDecimal("70000"), new BigDecimal("70000"), 1000000L,
            askPrice1, bidPrice1
        );

        // when
        BigDecimal spread = stockPrice.getSpread();

        // then
        assertThat(spread).isEqualByComparingTo(expectedSpread);
    }

    @Test
    @DisplayName("가격 업데이트 시 새로운 불변 인스턴스를 반환한다")
    void givenExistingStockPrice_whenUpdatePrice_thenReturnsNewImmutableInstance() {
        // given
        StockPrice originalStock = StockPrice.createWithTTL(
            "005930", "삼성전자", new BigDecimal("70000"), new BigDecimal("69000"),
            new BigDecimal("70000"), new BigDecimal("69000"), new BigDecimal("70000"), 1000000L,
            new BigDecimal("70100"), new BigDecimal("69900")
        );
        
        BigDecimal newPrice = new BigDecimal("71000");
        Long newVolume = 2000000L;
        BigDecimal newAskPrice = new BigDecimal("71100");
        BigDecimal newBidPrice = new BigDecimal("70900");

        // when
        StockPrice updatedStock = originalStock.updatePrice(newPrice, newVolume, newAskPrice, newBidPrice);

        // then
        assertThat(updatedStock).isNotSameAs(originalStock);
        assertThat(updatedStock.getCurrentPrice()).isEqualByComparingTo(newPrice);
        assertThat(updatedStock.getVolume()).isEqualTo(newVolume);
        assertThat(updatedStock.getAskPrice1()).isEqualByComparingTo(newAskPrice);
        assertThat(updatedStock.getBidPrice1()).isEqualByComparingTo(newBidPrice);
        assertThat(updatedStock.getSymbol()).isEqualTo(originalStock.getSymbol());
        assertThat(updatedStock.getTimestamp()).isAfter(originalStock.getTimestamp());
    }

    @Test
    @DisplayName("MarketDataValue 인터페이스를 올바르게 구현한다")
    void givenStockPrice_whenAccessMarketDataValue_thenImplementsInterfaceCorrectly() {
        // given
        StockPrice stockPrice = StockPrice.createWithTTL(
            "005930", "삼성전자", new BigDecimal("70000"), new BigDecimal("69000"),
            new BigDecimal("70000"), new BigDecimal("69000"), new BigDecimal("70000"), 1000000L,
            new BigDecimal("70100"), new BigDecimal("69900")
        );

        // when & then
        assertThat(stockPrice.getSymbol()).isEqualTo("005930");
        assertThat(stockPrice.getName()).isEqualTo("삼성전자");
        assertThat(stockPrice.getCurrentValue()).isEqualByComparingTo(new BigDecimal("70000"));
        assertThat(stockPrice.getChange()).isEqualByComparingTo(new BigDecimal("1000"));
        assertThat(stockPrice.getMarketDataType()).isEqualTo(MarketDataType.STOCK);
        assertThat(stockPrice.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("전일종가가 null인 경우 change와 changeRate가 0으로 처리된다")
    void givenNullPreviousClose_whenCreateStockPrice_thenChangeAndRateAreZero() {
        // given
        BigDecimal currentPrice = new BigDecimal("70000");
        BigDecimal previousClose = null;

        // when
        StockPrice stockPrice = StockPrice.createWithTTL(
            "005930", "삼성전자", currentPrice, previousClose,
            currentPrice, currentPrice, currentPrice, 1000000L,
            new BigDecimal("70100"), new BigDecimal("69900")
        );

        // then
        assertThat(stockPrice.getChange()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(stockPrice.getChangeRate()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(stockPrice.isPriceUp()).isFalse();
        assertThat(stockPrice.isPriceDown()).isFalse();
    }

    @Test
    @DisplayName("매도호가 또는 매수호가가 null인 경우 스프레드는 0으로 처리된다")
    void givenNullAskOrBidPrice_whenCalculateSpread_thenReturnsZero() {
        // given
        StockPrice stockPriceWithNullAsk = StockPrice.createWithTTL(
            "005930", "삼성전자", new BigDecimal("70000"), new BigDecimal("70000"),
            new BigDecimal("70000"), new BigDecimal("70000"), new BigDecimal("70000"), 1000000L,
            null, new BigDecimal("69900")
        );

        StockPrice stockPriceWithNullBid = StockPrice.createWithTTL(
            "005930", "삼성전자", new BigDecimal("70000"), new BigDecimal("70000"),
            new BigDecimal("70000"), new BigDecimal("70000"), new BigDecimal("70000"), 1000000L,
            new BigDecimal("70100"), null
        );

        // when
        BigDecimal spreadWithNullAsk = stockPriceWithNullAsk.getSpread();
        BigDecimal spreadWithNullBid = stockPriceWithNullBid.getSpread();

        // then
        assertThat(spreadWithNullAsk).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(spreadWithNullBid).isEqualByComparingTo(BigDecimal.ZERO);
    }
}