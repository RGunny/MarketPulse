package me.rgunny.marketpulse.event.marketdata.infrastructure.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.marketpulse.event.marketdata.application.port.out.shared.StockPort;
import me.rgunny.marketpulse.event.marketdata.domain.model.MarketType;
import me.rgunny.marketpulse.event.marketdata.domain.model.Stock;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Stock 데이터 초기화
 * 
 * TODO: 향후 KIS API 종목 마스터 조회 API 연동으로 대체
 * WatchTargetDataInitializer보다 먼저 실행되도록 @Order(1) 설정
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
@Order(1)
public class StockDataInitializer implements CommandLineRunner {
    
    private final StockPort stockPort;
    
    @Override
    public void run(String... args) {
        log.info("Starting Stock master data initialization...");
        
        initializeStocks()
            .doOnSuccess(unused -> log.info("Stock master data initialization completed"))
            .doOnError(error -> log.error("Failed to initialize stock data", error))
            .subscribe();
    }
    
    private Mono<Void> initializeStocks() {
        return stockPort.count()
            .flatMap(count -> {
                if (count > 0) {
                    log.info("Stock data already exists. Count: {}", count);
                    return Mono.empty();
                }
                
                log.info("Initializing stock master data...");
                return Flux.fromIterable(getInitialStocks())
                    .flatMap(stock -> stockPort.save(stock)
                        .doOnNext(saved -> log.debug("Saved stock: {} ({})", 
                            saved.getName(), saved.getSymbol())))
                    .then();
            });
    }
    
    private List<Stock> getInitialStocks() {
        return List.of(
            // KOSPI 대표 종목
            Stock.createStock(
                "005930", "삼성전자", "Samsung Electronics",
                MarketType.KOSPI, "33", "전기전자"
            ),
            Stock.createStock(
                "000660", "SK하이닉스", "SK Hynix",
                MarketType.KOSPI, "33", "전기전자"
            ),
            Stock.createStock(
                "005380", "현대차", "Hyundai Motor",
                MarketType.KOSPI, "34", "운수장비"
            ),
            Stock.createStock(
                "005490", "POSCO홀딩스", "POSCO Holdings",
                MarketType.KOSPI, "15", "철강금속"
            ),
            Stock.createStock(
                "035420", "NAVER", "NAVER",
                MarketType.KOSPI, "73", "서비스업"
            ),
            Stock.createStock(
                "000270", "기아", "Kia",
                MarketType.KOSPI, "34", "운수장비"
            ),
            Stock.createStock(
                "068270", "셀트리온", "Celltrion",
                MarketType.KOSPI, "24", "의약품"
            ),
            Stock.createStock(
                "051910", "LG화학", "LG Chem",
                MarketType.KOSPI, "22", "화학"
            ),
            
            // KOSDAQ 대표 종목
            Stock.createStock(
                "247540", "에코프로비엠", "EcoPro BM",
                MarketType.KOSDAQ, "33", "전기전자"
            ),
            Stock.createStock(
                "086520", "에코프로", "EcoPro",
                MarketType.KOSDAQ, "22", "화학"
            ),
            Stock.createStock(
                "028300", "HLB", "HLB",
                MarketType.KOSDAQ, "24", "의약품"
            ),
            Stock.createStock(
                "196170", "알테오젠", "Alteogen",
                MarketType.KOSDAQ, "24", "의약품"
            ),
            
            // ETF (KOSPI 시장에서 거래)
            Stock.createETF(
                "069500", "KODEX 200", "KODEX 200",
                MarketType.KOSPI
            ),
            Stock.createETF(
                "229200", "KODEX 코스닥150", "KODEX KOSDAQ150",
                MarketType.KOSPI
            ),
            Stock.createETF(
                "122630", "KODEX 레버리지", "KODEX Leverage",
                MarketType.KOSPI
            ),
            Stock.createETF(
                "252670", "KODEX 200선물인버스2X", "KODEX 200 Futures Inverse 2X",
                MarketType.KOSPI
            )
        );
    }
}