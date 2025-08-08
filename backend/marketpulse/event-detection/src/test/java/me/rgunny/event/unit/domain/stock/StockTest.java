package me.rgunny.event.unit.domain.stock;

import me.rgunny.event.marketdata.domain.model.MarketType;
import me.rgunny.event.marketdata.domain.model.Stock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Stock 도메인 엔티티 단위 테스트")
class StockTest {

    @Nested
    @DisplayName("생성자 테스트")
    class ConstructorTest {
        
        @Test
        @DisplayName("일반 주식 생성 시 모든 필드가 정상적으로 설정된다")
        void givenStockData_whenCreateStock_thenAllFieldsAreSet() {
            // given
            String symbol = "005930";
            String name = "삼성전자";
            String englishName = "Samsung Electronics";
            MarketType marketType = MarketType.KOSPI;
            String sectorCode = "33";
            String sectorName = "전기전자";

            // when
            Stock stock = Stock.createStock(symbol, name, englishName, marketType, sectorCode, sectorName);

            // then
            assertThat(stock).isNotNull();
            assertThat(stock.getSymbol()).isEqualTo(symbol);
            assertThat(stock.getName()).isEqualTo(name);
            assertThat(stock.getEnglishName()).isEqualTo(englishName);
            assertThat(stock.getMarketType()).isEqualTo(marketType);
            assertThat(stock.getSectorCode()).isEqualTo(sectorCode);
            assertThat(stock.getSectorName()).isEqualTo(sectorName);
            assertThat(stock.isActive()).isTrue();
            assertThat(stock.isETF()).isFalse();
            assertThat(stock.isSPAC()).isFalse();
            assertThat(stock.getListedDate()).isNotNull();
            assertThat(stock.getDelistedDate()).isNull();
            assertThat(stock.getCreatedAt()).isNotNull();
            assertThat(stock.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("ETF 생성 시 ETF 플래그가 true로 설정된다")
        void givenETFData_whenCreateETF_thenIsETFIsTrue() {
            // given
            String symbol = "069500";
            String name = "KODEX 200";
            String englishName = "KODEX 200 ETF";
            MarketType marketType = MarketType.KOSPI;

            // when
            Stock etf = Stock.createETF(symbol, name, englishName, marketType);

            // then
            assertThat(etf).isNotNull();
            assertThat(etf.getSymbol()).isEqualTo(symbol);
            assertThat(etf.getName()).isEqualTo(name);
            assertThat(etf.isETF()).isTrue();
            assertThat(etf.isSPAC()).isFalse();
            assertThat(etf.isActive()).isTrue();
            assertThat(etf.getSectorCode()).isEqualTo("ETF");
            assertThat(etf.getSectorName()).isEqualTo("ETF");
        }
    }

    @Nested
    @DisplayName("비즈니스 메서드 테스트")
    class BusinessMethodTest {
        
        @Test
        @DisplayName("활성 상태이고 상장폐지일이 없으면 거래 가능하다.")
        void givenActiveAndNotDelisted_whenIsTradable_thenReturnsTrue() {
            // given
            Stock stock = Stock.createStock("005930", "삼성전자", "Samsung Electronics",
                    MarketType.KOSPI, "33", "전기전자");

            // when
            boolean tradable = stock.isTradable();

            // then
            assertThat(tradable).isTrue();
        }

        @Test
        @DisplayName("상장폐지 처리 시 거래 불가능 상태가 된다.")
        void givenActiveStock_whenDelist_thenNotTradable() {
            // given
            Stock stock = Stock.createStock("005930", "삼성전자", "Samsung Electronics",
                    MarketType.KOSPI, "33", "전기전자");

            // when
            Stock delistedStock = stock.delist();

            // then
            assertThat(delistedStock.isTradable()).isFalse();
            assertThat(delistedStock.isActive()).isFalse();
            assertThat(delistedStock.getDelistedDate()).isNotNull();
            assertThat(delistedStock.getUpdatedAt()).isAfter(stock.getCreatedAt());
        }

        @Test
        @DisplayName("종목 정보 업데이트 시 새로운 인스턴스가 생성된다.")
        void givenStock_whenUpdateInfo_thenReturnsNewInstance() {
            // given
            Stock stock = Stock.createStock("005930", "삼성전자", "Samsung Electronics",
                    MarketType.KOSPI, "33", "전기전자");
            String newName = "삼성전자(우)";
            String newSectorCode = "34";
            String newSectorName = "전기전자우선주";

            // when
            Stock updatedStock = stock.updateInfo(newName, newSectorCode, newSectorName, true);

            // then
            assertThat(updatedStock).isNotSameAs(stock);  // 다른 인스턴스
            assertThat(updatedStock.getName()).isEqualTo(newName);
            assertThat(updatedStock.getSectorCode()).isEqualTo(newSectorCode);
            assertThat(updatedStock.getSectorName()).isEqualTo(newSectorName);
            assertThat(updatedStock.getSymbol()).isEqualTo(stock.getSymbol());  // 종목코드는 불변
            assertThat(updatedStock.getUpdatedAt()).isAfter(stock.getUpdatedAt());
        }

        @Test
        @DisplayName("KOSPI 또는 KOSDAQ 종목은 주요 종목으로 분류된다.")
        void givenKospiOrKosdaqStock_whenIsMajorStock_thenReturnsTrue() {
            // given
            Stock kospiStock = Stock.createStock("005930", "삼성전자", "Samsung Electronics",
                    MarketType.KOSPI, "33", "전기전자");
            Stock kosdaqStock = Stock.createStock("035720", "카카오", "Kakao",
                    MarketType.KOSDAQ, "58", "서비스업");
            Stock konexStock = Stock.createStock("900000", "테스트", "Test",
                    MarketType.KONEX, "99", "기타");

            // when & then
            assertThat(kospiStock.isMajorStock()).isTrue();
            assertThat(kosdaqStock.isMajorStock()).isTrue();
            assertThat(konexStock.isMajorStock()).isFalse();
        }
    }

    @Nested
    @DisplayName("MarketType Enum 테스트")
    class MarketTypeTest {
        
        @Test
        @DisplayName("시장 구분별 설명이 올바르게 반환된다.")
        void givenMarketType_whenGetDescription_thenReturnsCorrectDescription() {
            // given & when & then
            assertThat(MarketType.KOSPI.getDescription()).isEqualTo("코스피");
            assertThat(MarketType.KOSDAQ.getDescription()).isEqualTo("코스닥");
            assertThat(MarketType.KONEX.getDescription()).isEqualTo("코넥스");
            assertThat(MarketType.NASDAQ.getDescription()).isEqualTo("나스닥");
            assertThat(MarketType.NYSE.getDescription()).isEqualTo("뉴욕증권거래소");
        }

        @Test
        @DisplayName("모든 MarketType enum 값이 정의되어 있다.")
        void allMarketTypesAreDefined() {
            // given & when
            MarketType[] marketTypes = MarketType.values();

            // then
            assertThat(marketTypes).hasSize(5);
            assertThat(marketTypes).containsExactlyInAnyOrder(
                MarketType.KOSPI,
                MarketType.KOSDAQ,
                MarketType.KONEX,
                MarketType.NASDAQ,
                MarketType.NYSE
            );
        }
    }

    @Nested
    @DisplayName("불변성 테스트")
    class ImmutabilityTest {
        
        @Test
        @DisplayName("Stock 엔티티는 불변 객체로 모든 필드가 final이다")
        void stockIsImmutable() {
            // given
            Stock stock = Stock.createStock("005930", "삼성전자", "Samsung Electronics",
                    MarketType.KOSPI, "33", "전기전자");

            // when & then
            assertThat(stock.getSymbol()).isEqualTo("005930");
            assertThat(stock.getName()).isEqualTo("삼성전자");
            assertThat(stock.getMarketType()).isEqualTo(MarketType.KOSPI);
            
            // setter 메서드가 없음을 간접적으로 확인
            assertThat(Stock.class.getMethods())
                .filteredOn(method -> method.getName().startsWith("set"))
                .isEmpty();
        }
    }
}