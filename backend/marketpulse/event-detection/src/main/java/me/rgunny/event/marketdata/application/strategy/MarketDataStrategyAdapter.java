package me.rgunny.event.marketdata.application.strategy;

import lombok.RequiredArgsConstructor;
import me.rgunny.event.marketdata.application.port.out.ExternalApiPort;
import me.rgunny.event.shared.domain.value.MarketDataType;
import me.rgunny.event.shared.domain.value.MarketDataValue;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class MarketDataStrategyAdapter implements ExternalApiPort {

    private final MarketDataStrategyRegistry registry;

    @Override
    public boolean supports(MarketDataType dataType) {
        return dataType == MarketDataType.STOCK;
    }

    @Override
    public <T extends MarketDataValue> Mono<T> fetchMarketData(String symbol, MarketDataType dataType, Class<T> clazz) {
        return registry.fetch(dataType, symbol, clazz);
    }

    @Override
    public String getProviderName() {
        return "KIS";
    }

    @Override
    public int getRateLimitPerMinute() {
        return 200; // KIS API 제한
    }

}
