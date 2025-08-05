package me.rgunny.event.marketdata.application.strategy;

import me.rgunny.event.shared.domain.value.MarketDataType;
import me.rgunny.event.shared.domain.value.MarketDataValue;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class MarketDataStrategyRegistry {

    private final Map<MarketDataType, MarketDataStrategy> strategyMap;

    public MarketDataStrategyRegistry(List<MarketDataStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toUnmodifiableMap(MarketDataStrategy::getType, Function.identity()));
    }

    public <T extends MarketDataValue> Mono<T> fetch(MarketDataType type, String symbol, Class<T> clazz) {
        MarketDataStrategy strategy = strategyMap.get(type);

        if (strategy == null) {
            return Mono.error(new IllegalArgumentException("No strategy registered for type " + type + " and symbol " + symbol));
        }

        return strategy.fetch(symbol, clazz);
    }
}
