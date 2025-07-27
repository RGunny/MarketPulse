package me.rgunny.event.unit.infrastructure.adapter.output;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.rgunny.event.domain.stock.StockPrice;
import me.rgunny.event.fixture.StockPriceTestFixture;
import me.rgunny.event.infrastructure.adapter.output.RedisStockPriceCacheAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisStockPriceCacheAdapter 단위 테스트")
class RedisStockPriceCacheAdapterTest {
    
    @Mock
    private ReactiveRedisTemplate<String, String> redisTemplate;
    
    @Mock
    private ReactiveValueOperations<String, String> valueOperations;
    
    @Mock
    private ObjectMapper objectMapper;
    
    private RedisStockPriceCacheAdapter cacheAdapter;
    
    private static final String SYMBOL = "005930";
    private static final String CACHE_KEY = "stock:price:" + SYMBOL;
    
    @BeforeEach
    void setUp() {
        cacheAdapter = new RedisStockPriceCacheAdapter(redisTemplate, objectMapper);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }
    
    @Test
    @DisplayName("캐시에서 StockPrice 조회 성공")
    void givenCachedData_whenGetStockPrice_thenReturnsStockPrice() throws JsonProcessingException {
        // given
        StockPrice stockPrice = createSampleStockPrice();
        String serializedData = "{\"symbol\":\"005930\",\"currentPrice\":71000}";
        
        given(valueOperations.get(CACHE_KEY)).willReturn(Mono.just(serializedData));
        given(objectMapper.readValue(serializedData, StockPrice.class)).willReturn(stockPrice);
        
        // when
        Mono<StockPrice> result = cacheAdapter.getStockPrice(SYMBOL);
        
        // then
        StepVerifier.create(result)
                .assertNext(retrieved -> {
                    assertThat(retrieved.getSymbol()).isEqualTo(SYMBOL);
                    assertThat(retrieved.getCurrentPrice()).isEqualByComparingTo(new BigDecimal("71000"));
                })
                .verifyComplete();
        
        verify(valueOperations).get(CACHE_KEY);
        verify(objectMapper).readValue(serializedData, StockPrice.class);
    }
    
    @Test
    @DisplayName("캐시에 데이터가 없으면 empty 반환")
    void givenNoData_whenGetStockPrice_thenReturnsEmpty() {
        // given
        given(valueOperations.get(CACHE_KEY)).willReturn(Mono.empty());
        
        // when
        Mono<StockPrice> result = cacheAdapter.getStockPrice(SYMBOL);
        
        // then
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(valueOperations).get(CACHE_KEY);
    }
    
    @Test
    @DisplayName("StockPrice 캐시 저장 성공")
    void givenStockPrice_whenSaveStockPrice_thenSavesToCache() throws JsonProcessingException {
        // given
        StockPrice stockPrice = createSampleStockPrice();
        String serializedData = "{\"symbol\":\"005930\",\"currentPrice\":71000}";
        Duration ttl = Duration.ofMinutes(1);
        
        given(objectMapper.writeValueAsString(stockPrice)).willReturn(serializedData);
        given(valueOperations.set(CACHE_KEY, serializedData, ttl)).willReturn(Mono.just(true));
        
        // when
        Mono<Void> result = cacheAdapter.saveStockPrice(stockPrice, ttl);
        
        // then
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(objectMapper).writeValueAsString(stockPrice);
        verify(valueOperations).set(CACHE_KEY, serializedData, ttl);
    }
    
    @Test
    @DisplayName("캐시 삭제 성공")
    void givenSymbol_whenDeleteStockPrice_thenDeletesFromCache() {
        // given
        given(redisTemplate.delete(CACHE_KEY)).willReturn(Mono.just(1L));
        
        // when
        Mono<Void> result = cacheAdapter.deleteStockPrice(SYMBOL);
        
        // then
        StepVerifier.create(result)
                .verifyComplete();
    }
    
    @Test
    @DisplayName("TTL 조회 성공")
    void givenSymbol_whenGetStockPriceTtl_thenReturnsTtl() {
        // given
        Duration ttl = Duration.ofMinutes(5);
        given(redisTemplate.getExpire(CACHE_KEY)).willReturn(Mono.just(ttl));
        
        // when
        Mono<Long> result = cacheAdapter.getStockPriceTtl(SYMBOL);
        
        // then
        StepVerifier.create(result)
                .expectNext(300L) // 5분 = 300초
                .verifyComplete();
    }
    
    @Test
    @DisplayName("직렬화 실패 시 빈 Mono 반환")
    void givenSerializationError_whenSaveStockPrice_thenReturnsEmpty() throws JsonProcessingException {
        // given
        StockPrice stockPrice = createSampleStockPrice();
        Duration ttl = Duration.ofMinutes(1);
        
        given(objectMapper.writeValueAsString(stockPrice))
                .willThrow(new JsonProcessingException("Serialization failed") {});
        
        // when
        Mono<Void> result = cacheAdapter.saveStockPrice(stockPrice, ttl);
        
        // then
        StepVerifier.create(result)
                .verifyComplete(); // 에러가 발생해도 빈 Mono로 완료됨
    }
    
    @Test
    @DisplayName("역직렬화 실패 시 빈 Mono 반환")
    void givenDeserializationError_whenGetStockPrice_thenReturnsEmpty() throws JsonProcessingException {
        // given
        String serializedData = "invalid-json";
        
        given(valueOperations.get(CACHE_KEY)).willReturn(Mono.just(serializedData));
        given(objectMapper.readValue(serializedData, StockPrice.class))
                .willThrow(new JsonProcessingException("Deserialization failed") {});
        
        // when
        Mono<StockPrice> result = cacheAdapter.getStockPrice(SYMBOL);
        
        // then
        StepVerifier.create(result)
                .verifyComplete(); // 에러가 발생해도 빈 Mono로 완료됨
    }
    
    @Test
    @DisplayName("Redis 연결 실패 시 적절히 처리")
    void givenRedisError_whenGetStockPrice_thenHandlesGracefully() {
        // given
        given(valueOperations.get(CACHE_KEY))
                .willReturn(Mono.error(new RuntimeException("Redis connection failed")));
        
        // when
        Mono<StockPrice> result = cacheAdapter.getStockPrice(SYMBOL);
        
        // then
        StepVerifier.create(result)
                .verifyComplete(); // 에러가 발생해도 empty로 처리됨
    }
    
    @Test
    @DisplayName("캐시 저장 실패 시 적절히 처리")
    void givenRedisError_whenSaveStockPrice_thenHandlesGracefully() throws JsonProcessingException {
        // given
        StockPrice stockPrice = createSampleStockPrice();
        String serializedData = "{\"symbol\":\"005930\",\"currentPrice\":71000}";
        Duration ttl = Duration.ofMinutes(1);
        
        given(objectMapper.writeValueAsString(stockPrice)).willReturn(serializedData);
        given(valueOperations.set(CACHE_KEY, serializedData, ttl))
                .willReturn(Mono.error(new RuntimeException("Redis connection failed")));
        
        // when
        Mono<Void> result = cacheAdapter.saveStockPrice(stockPrice, ttl);
        
        // then
        StepVerifier.create(result)
                .verifyComplete(); // 에러가 발생해도 빈 Mono로 처리됨
    }
    
    @Test
    @DisplayName("캐시 삭제 실패 시 적절히 처리")
    void givenRedisError_whenDeleteStockPrice_thenHandlesGracefully() {
        // given
        given(redisTemplate.delete(CACHE_KEY))
                .willReturn(Mono.error(new RuntimeException("Redis connection failed")));
        
        // when
        Mono<Void> result = cacheAdapter.deleteStockPrice(SYMBOL);
        
        // then
        StepVerifier.create(result)
                .verifyComplete(); // 에러가 발생해도 처리됨
    }
    
    private StockPrice createSampleStockPrice() {
        return StockPrice.createWithTTL(
                SYMBOL,
                "삼성전자",
                new BigDecimal("71000"),    // 현재가
                new BigDecimal("70000"),    // 전일종가
                new BigDecimal("71500"),    // 고가
                new BigDecimal("70500"),    // 저가
                new BigDecimal("70800"),    // 시가
                1000000L,                   // 거래량
                new BigDecimal("71100"),    // 매도호가1
                new BigDecimal("70900")     // 매수호가1
        );
    }
}