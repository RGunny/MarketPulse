package me.rgunny.event.infrastructure.adapter.output;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.rgunny.event.application.port.output.StockPriceCachePort;
import me.rgunny.event.domain.stock.StockPrice;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Component
public class RedisStockPriceCacheAdapter implements StockPriceCachePort {
    private static final String STOCK_PRICE_KEY_PREFIX = "stock:price:";
    
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    public RedisStockPriceCacheAdapter(@Qualifier("reactiveStringRedisTemplate") ReactiveRedisTemplate<String, String> redisTemplate,
                                      ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }
    
    @Override
    public Mono<StockPrice> getStockPrice(String symbol) {
        String key = STOCK_PRICE_KEY_PREFIX + symbol;
        
        return redisTemplate.opsForValue().get(key)
                .flatMap(this::deserializeStockPrice)
                .doOnNext(stockPrice -> log.debug("Cache hit for symbol: {}", symbol))
                .doOnSuccess(result -> {
                    if (result == null) {
                        log.debug("Cache miss for symbol: {}", symbol);
                    }
                })
                .onErrorResume(error -> {
                    log.warn("Failed to get StockPrice from cache for symbol: {}", symbol, error);
                    return Mono.empty();
                });
    }
    
    @Override
    public Mono<Void> saveStockPrice(StockPrice stockPrice, Duration ttl) {
        String key = STOCK_PRICE_KEY_PREFIX + stockPrice.getSymbol();
        
        return serializeStockPrice(stockPrice)
                .flatMap(serialized -> redisTemplate.opsForValue().set(key, serialized, ttl))
                .doOnSuccess(success -> log.debug("Cached StockPrice for symbol: {} with TTL: {}", 
                        stockPrice.getSymbol(), ttl))
                .onErrorResume(error -> {
                    log.warn("Failed to save StockPrice to cache for symbol: {}", 
                            stockPrice.getSymbol(), error);
                    return Mono.empty();
                })
                .then();
    }
    
    @Override
    public Mono<Void> deleteStockPrice(String symbol) {
        String key = STOCK_PRICE_KEY_PREFIX + symbol;
        
        return redisTemplate.delete(key)
                .doOnNext(deletedCount -> log.debug("Deleted {} cache entries for symbol: {}", 
                        deletedCount, symbol))
                .onErrorResume(error -> {
                    log.warn("Failed to delete StockPrice cache for symbol: {}", symbol, error);
                    return Mono.just(0L);
                })
                .then();
    }
    
    @Override
    public Mono<Long> getStockPriceTtl(String symbol) {
        String key = STOCK_PRICE_KEY_PREFIX + symbol;
        
        return redisTemplate.getExpire(key)
                .map(Duration::getSeconds)
                .onErrorResume(error -> {
                    log.warn("Failed to get TTL for symbol: {}", symbol, error);
                    return Mono.just(-1L);
                });
    }
    
    private Mono<String> serializeStockPrice(StockPrice stockPrice) {
        return Mono.fromCallable(() -> {
            try {
                return objectMapper.writeValueAsString(stockPrice);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to serialize StockPrice", e);
            }
        });
    }
    
    private Mono<StockPrice> deserializeStockPrice(String json) {
        return Mono.fromCallable(() -> {
            try {
                return objectMapper.readValue(json, StockPrice.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to deserialize StockPrice", e);
            }
        });
    }
}