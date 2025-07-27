package me.rgunny.event.medium.infrastructure.config;

import me.rgunny.event.domain.stock.StockPrice;
import me.rgunny.event.infrastructure.repository.StockPriceRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MongoDB 연결 및 데이터 액세스 Medium 테스트
 * 
 * - Spring Boot 테스트 슬라이스 사용 (@DataMongoTest)
 * - TestContainers로 실제 MongoDB 환경 시뮬레이션
 * - Repository 계층과 데이터베이스 통합 검증
 */
@DataMongoTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("MongoDB 연결 테스트 (medium)")
class MongoConnectionTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0")
            .withExposedPorts(27017);

    @Autowired
    private ReactiveMongoTemplate mongoTemplate;

    @Autowired
    private StockPriceRepository stockPriceRepository;

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Test
    @DisplayName("MongoDB에 연결할 수 있다")
    void givenMongoDBContainer_whenGettingCollectionNames_thenReturnsSuccessMessage() {
        // given
        // MongoDB TestContainer가 실행 중
        
        // when
        Mono<String> result = mongoTemplate.getCollectionNames()
                .collectList()
                .map(collections -> "Connected to MongoDB with collections: " + collections);

        // then
        StepVerifier.create(result)
                .expectNextMatches(message -> message.contains("Connected to MongoDB"))
                .verifyComplete();
    }

    @Test
    @DisplayName("StockPrice 엔티티를 저장하고 조회할 수 있다")
    void givenStockPriceEntity_whenSaveAndFindById_thenReturnsCorrectData() {
        // given
        StockPrice stockPrice = StockPrice.builder()
                .symbol("005930")
                .name("삼성전자")
                .currentPrice(new BigDecimal("70000"))
                .previousClose(new BigDecimal("70500"))
                .change(new BigDecimal("-500"))
                .changeRate(new BigDecimal("-0.71"))
                .volume(12345678L)
                .timestamp(LocalDateTime.now())
                .build();

        // when
        Mono<StockPrice> savedAndFound = stockPriceRepository.save(stockPrice)
                .flatMap(saved -> stockPriceRepository.findById(saved.getId()));

        // then
        StepVerifier.create(savedAndFound)
                .assertNext(found -> {
                    assertThat(found.getSymbol()).isEqualTo("005930");
                    assertThat(found.getName()).isEqualTo("삼성전자");
                    assertThat(found.getCurrentPrice()).isEqualByComparingTo("70000");
                    assertThat(found.getVolume()).isEqualTo(12345678L);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("종목 코드로 최신 시세를 조회할 수 있다")
    void givenMultipleStockPricesWithSameSymbol_whenFindLatestBySymbol_thenReturnsNewestPrice() {
        // given
        String symbol = "000660";
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS);

        StockPrice older = StockPrice.builder()
                .symbol(symbol)
                .name("SK하이닉스")
                .currentPrice(new BigDecimal("120000"))
                .timestamp(now.minusMinutes(5))
                .build();

        StockPrice latest = StockPrice.builder()
                .symbol(symbol)
                .name("SK하이닉스")
                .currentPrice(new BigDecimal("121000"))
                .timestamp(now)
                .build();

        // when
        Mono<StockPrice> result = stockPriceRepository.save(older)
                .then(stockPriceRepository.save(latest))
                .then(stockPriceRepository.findFirstBySymbolOrderByTimestampDesc(symbol));

        // then
        StepVerifier.create(result)
                .assertNext(found -> {
                    assertThat(found.getCurrentPrice()).isEqualByComparingTo("121000");
                    assertThat(found.getTimestamp().truncatedTo(ChronoUnit.MILLIS))
                            .isEqualTo(now);
                })
                .verifyComplete();
    }
}