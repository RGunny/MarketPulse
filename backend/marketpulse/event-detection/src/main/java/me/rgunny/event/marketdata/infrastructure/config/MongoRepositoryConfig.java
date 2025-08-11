package me.rgunny.event.marketdata.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

/**
 * Event Detection 모듈 MongoDB Repository 설정
 * 
 * 해당 서비스에서만 사용하는 Repository 스캔 범위 지정:
 * - marketdata 도메인: Stock, StockPrice Repository (shared 패키지)
 * - watchlist 도메인: WatchTarget Repository (persistence 패키지)
 */
@Configuration
@EnableReactiveMongoRepositories(basePackages = {
    "me.rgunny.event.marketdata.infrastructure.adapter.out.shared",
    "me.rgunny.event.watchlist.infrastructure.adapter.out.persistence"
})
public class MongoRepositoryConfig {
}