package me.rgunny.marketpulse.event.watchlist.infrastructure.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.marketpulse.event.marketdata.application.port.out.shared.StockPort;
import me.rgunny.marketpulse.event.watchlist.application.port.out.WatchTargetPort;
import me.rgunny.marketpulse.event.watchlist.domain.model.WatchCategory;
import me.rgunny.marketpulse.event.watchlist.domain.model.WatchTarget;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * WatchTarget 초기 데이터 생성
 * 
 * Stock 마스터 데이터를 기반으로 감시 대상 종목을 설정합니다.
 * StockDataInitializer 이후에 실행되도록 @Order(2) 설정
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
@Order(2)
public class WatchTargetDataInitializer implements CommandLineRunner {
    
    private final WatchTargetPort watchTargetPort;
    private final StockPort stockPort;
    
    @Override
    public void run(String... args) {
        log.info("Starting WatchTarget initialization...");
        
        initializeWatchTargets()
            .doOnSuccess(count -> log.info("WatchTarget initialization completed. Created {} targets", count))
            .doOnError(error -> log.error("Failed to initialize WatchTarget data", error))
            .subscribe();
    }
    
    private Mono<Long> initializeWatchTargets() {
        // 이미 데이터가 있는지 확인
        return watchTargetPort.findActiveTargets()
            .count()
            .flatMap(count -> {
                if (count > 0) {
                    log.info("WatchTarget data already exists. Count: {}", count);
                    return Mono.just(count);
                }
                
                log.info("Creating initial WatchTarget data...");
                return createInitialWatchTargets();
            });
    }
    
    private Mono<Long> createInitialWatchTargets() {
        // 종목별 카테고리와 우선순위 매핑
        Map<String, WatchTargetConfig> configMap = Map.ofEntries(
            // CORE 카테고리 - 대형주, 우선순위 높음
            Map.entry("005930", new WatchTargetConfig(WatchCategory.CORE, 1, 30)),  // 삼성전자
            Map.entry("000660", new WatchTargetConfig(WatchCategory.CORE, 1, 30)),  // SK하이닉스
            Map.entry("005380", new WatchTargetConfig(WatchCategory.CORE, 2, 30)),  // 현대차
            Map.entry("000270", new WatchTargetConfig(WatchCategory.CORE, 2, 30)),  // 기아
            Map.entry("035420", new WatchTargetConfig(WatchCategory.CORE, 2, 30)),  // NAVER
            
            // THEME 카테고리 - 테마주, 중간 우선순위
            Map.entry("247540", new WatchTargetConfig(WatchCategory.THEME, 3, 60)), // 에코프로비엠
            Map.entry("086520", new WatchTargetConfig(WatchCategory.THEME, 3, 60)), // 에코프로
            Map.entry("068270", new WatchTargetConfig(WatchCategory.THEME, 4, 60)), // 셀트리온
            Map.entry("051910", new WatchTargetConfig(WatchCategory.THEME, 4, 60)), // LG화학
            Map.entry("005490", new WatchTargetConfig(WatchCategory.THEME, 4, 60)), // POSCO홀딩스
            
            // MOMENTUM 카테고리 - 모멘텀 종목, 낮은 우선순위
            Map.entry("028300", new WatchTargetConfig(WatchCategory.MOMENTUM, 5, 120)), // HLB
            Map.entry("196170", new WatchTargetConfig(WatchCategory.MOMENTUM, 5, 120)), // 알테오젠
            
            // ETF - CORE 카테고리, 중간 우선순위
            Map.entry("069500", new WatchTargetConfig(WatchCategory.CORE, 3, 60)),   // KODEX 200
            Map.entry("229200", new WatchTargetConfig(WatchCategory.CORE, 3, 60)),   // KODEX 코스닥150
            Map.entry("122630", new WatchTargetConfig(WatchCategory.MOMENTUM, 5, 120)), // KODEX 레버리지
            Map.entry("252670", new WatchTargetConfig(WatchCategory.MOMENTUM, 5, 120))  // KODEX 인버스2X
        );
        
        return stockPort.findAllActiveStocks()
            .filter(stock -> configMap.containsKey(stock.getSymbol()))
            .flatMap(stock -> {
                WatchTargetConfig config = configMap.get(stock.getSymbol());
                WatchTarget target = createWatchTarget(stock.getSymbol(), stock.getName(), config);
                
                return watchTargetPort.save(target)
                    .doOnNext(saved -> log.debug("Created WatchTarget: {} ({}) - Category: {}, Priority: {}", 
                        saved.getName(), saved.getSymbol(), saved.getCategory(), saved.getPriority()));
            })
            .count();
    }
    
    private WatchTarget createWatchTarget(String symbol, String name, WatchTargetConfig config) {
        LocalDateTime now = LocalDateTime.now();
        String description = String.format("%s 카테고리 - 우선순위 %d, %d초 주기", 
            config.category, config.priority, config.collectIntervalSeconds);
        
        return new WatchTarget(
            null, // ID는 MongoDB가 자동 생성
            symbol,
            name,
            config.category,
            null, // tags
            config.priority,
            config.collectIntervalSeconds,
            true, // active
            description,
            now,
            now
        );
    }
    
    /**
     * WatchTarget 설정 정보
     */
    private record WatchTargetConfig(
        WatchCategory category,
        int priority,
        int collectIntervalSeconds
    ) {}
}