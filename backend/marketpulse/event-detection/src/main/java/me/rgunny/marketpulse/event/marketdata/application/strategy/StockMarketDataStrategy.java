package me.rgunny.marketpulse.event.marketdata.application.strategy;

import lombok.RequiredArgsConstructor;
import me.rgunny.marketpulse.event.marketdata.infrastructure.adapter.out.kis.KISApiService;
import me.rgunny.marketpulse.event.marketdata.domain.error.StockPriceErrorCode;
import me.rgunny.marketpulse.event.marketdata.domain.model.StockPrice;
import me.rgunny.marketpulse.event.shared.domain.value.MarketDataType;
import me.rgunny.marketpulse.event.shared.domain.value.MarketDataValue;
import me.rgunny.marketpulse.common.core.exception.BusinessException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class StockMarketDataStrategy implements MarketDataStrategy {

    private final KISApiService kisApiService;

    @Override
    public MarketDataType getType() {
        return MarketDataType.STOCK;
    }

    @Override
    public <T extends MarketDataValue> Mono<T> fetch(String symbol, Class<T> clazz) {
        if (!clazz.isAssignableFrom(StockPrice.class)) {
            return Mono.error(new BusinessException(StockPriceErrorCode.STOCK_PRICE_999));
        }
        return kisApiService.fetchCurrentPrice(symbol).cast(clazz);
    }
}
