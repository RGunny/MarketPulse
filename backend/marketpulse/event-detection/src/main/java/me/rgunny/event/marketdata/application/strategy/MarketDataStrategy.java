package me.rgunny.event.marketdata.application.strategy;

import me.rgunny.event.shared.domain.value.MarketDataType;
import me.rgunny.event.shared.domain.value.MarketDataValue;
import reactor.core.publisher.Mono;

public interface MarketDataStrategy {

    MarketDataType getType();
    <T extends MarketDataValue> Mono<T> fetch(String symbol, Class<T> clazz);
}
