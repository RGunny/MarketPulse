package me.rgunny.event.unit.domain.stock;

import me.rgunny.event.marketdata.domain.error.StockErrorCode;
import me.rgunny.event.marketdata.domain.model.MarketType;
import me.rgunny.event.marketdata.domain.model.Stock;
import me.rgunny.marketpulse.common.core.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Stock 도메인 검증 로직 단위 테스트")
class StockValidationTest {

    @Nested
    @DisplayName("필수값 검증")
    class RequiredFieldValidationTest {
        
        @Test
        @DisplayName("종목코드가 null이면 BusinessException이 발생한다")
        void givenNullSymbol_whenCreateStock_thenThrowsBusinessException() {
            // given
            String symbol = null;
            String name = "삼성전자";
            MarketType marketType = MarketType.KOSPI;
            
            // when & then
            assertThatThrownBy(() -> new Stock(
                    null, symbol, name, "Samsung", marketType, "33", "전기전자",
                    true, false, false, LocalDateTime.now(), null, 
                    LocalDateTime.now(), LocalDateTime.now()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", StockErrorCode.STOCK_SYMBOL_REQUIRED);
        }
        
        @Test
        @DisplayName("종목코드가 빈 문자열이면 BusinessException이 발생한다")
        void givenEmptySymbol_whenCreateStock_thenThrowsBusinessException() {
            // given
            String symbol = "   ";
            String name = "삼성전자";
            MarketType marketType = MarketType.KOSPI;
            
            // when & then
            assertThatThrownBy(() -> new Stock(
                    null, symbol, name, "Samsung", marketType, "33", "전기전자",
                    true, false, false, LocalDateTime.now(), null,
                    LocalDateTime.now(), LocalDateTime.now()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", StockErrorCode.STOCK_SYMBOL_REQUIRED);
        }
        
        @Test
        @DisplayName("종목명이 null이면 BusinessException이 발생한다")
        void givenNullName_whenCreateStock_thenThrowsBusinessException() {
            // given
            String symbol = "005930";
            String name = null;
            MarketType marketType = MarketType.KOSPI;
            
            // when & then
            assertThatThrownBy(() -> new Stock(
                    null, symbol, name, "Samsung", marketType, "33", "전기전자",
                    true, false, false, LocalDateTime.now(), null,
                    LocalDateTime.now(), LocalDateTime.now()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", StockErrorCode.STOCK_NAME_REQUIRED);
        }
        
        @Test
        @DisplayName("시장구분이 null이면 BusinessException이 발생한다")
        void givenNullMarketType_whenCreateStock_thenThrowsBusinessException() {
            // given
            String symbol = "005930";
            String name = "삼성전자";
            MarketType marketType = null;
            
            // when & then
            assertThatThrownBy(() -> new Stock(
                    null, symbol, name, "Samsung", marketType, "33", "전기전자",
                    true, false, false, LocalDateTime.now(), null,
                    LocalDateTime.now(), LocalDateTime.now()))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", StockErrorCode.STOCK_MARKET_TYPE_REQUIRED);
        }
    }
    
    @Nested
    @DisplayName("종목코드 형식 검증")
    class SymbolFormatValidationTest {
        
        @Test
        @DisplayName("종목코드가 6자리 숫자가 아니면 BusinessException이 발생한다")
        void givenInvalidSymbolFormat_whenCreateStock_thenThrowsBusinessException() {
            // given
            String[] invalidSymbols = {"00593", "0059300", "A05930", "005-30", "005 930"};
            
            for (String symbol : invalidSymbols) {
                // when & then
                assertThatThrownBy(() -> new Stock(
                        null, symbol, "테스트", "Test", MarketType.KOSPI, "33", "전기전자",
                        true, false, false, LocalDateTime.now(), null,
                        LocalDateTime.now(), LocalDateTime.now()))
                        .isInstanceOf(BusinessException.class)
                        .hasFieldOrPropertyWithValue("errorCode", StockErrorCode.STOCK_INVALID_SYMBOL_FORMAT)
                        .as("종목코드 '%s'는 6자리 숫자 형식이 아닙니다", symbol);
            }
        }
        
        @Test
        @DisplayName("종목코드가 정확히 6자리 숫자면 정상 생성된다")
        void givenValidSymbolFormat_whenCreateStock_thenSuccess() {
            // given
            String[] validSymbols = {"005930", "035720", "000660", "999999"};
            
            for (String symbol : validSymbols) {
                // when & then (예외가 발생하지 않아야 함)
                Stock stock = new Stock(
                        null, symbol, "테스트", "Test", MarketType.KOSPI, "33", "전기전자",
                        true, false, false, LocalDateTime.now(), null,
                        LocalDateTime.now(), LocalDateTime.now());
                
                assertThat(stock.getSymbol()).isEqualTo(symbol);
            }
        }
    }
}