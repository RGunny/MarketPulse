package me.rgunny.event;

import me.rgunny.event.marketdata.infrastructure.config.kis.KISApiProperties;
import me.rgunny.event.marketdata.infrastructure.config.shared.StockCollectionProperties;
import me.rgunny.event.marketdata.infrastructure.config.PriceAlertProperties;
import me.rgunny.event.watchlist.infrastructure.config.WatchlistProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableConfigurationProperties({
        KISApiProperties.class,
        StockCollectionProperties.class,
        WatchlistProperties.class,
        PriceAlertProperties.class
})
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class EventDetectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventDetectionApplication.class, args);
    }
}
