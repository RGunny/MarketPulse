package me.rgunny.event.marketdata.application.strategy;

import lombok.RequiredArgsConstructor;
import me.rgunny.event.marketdata.domain.error.StockPriceErrorCode;
import me.rgunny.event.marketdata.domain.model.StockPrice;
import me.rgunny.event.marketdata.infrastructure.adapter.out.kis.KISApiAdapter;
import me.rgunny.event.shared.domain.value.MarketDataType;
import me.rgunny.event.shared.domain.value.MarketDataValue;
import me.rgunny.marketpulse.common.core.exception.BusinessException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class StockMarketDataStrategy implements MarketDataStrategy {

    private final KISApiAdapter kisApiAdapter;

    @Override
    public MarketDataType getType() {
        return MarketDataType.STOCK;
    }

    @Override
    public <T extends MarketDataValue> Mono<T> fetch(String symbol, Class<T> clazz) {
        if (!clazz.isAssignableFrom(StockPrice.class)) {
            return Mono.error(new BusinessException(StockPriceErrorCode.STOCK_PRICE_999));
        }
        return kisApiAdapter.getCurrentPrice(symbol).cast(clazz);
    }
}
