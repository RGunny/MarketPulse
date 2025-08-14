package me.rgunny.marketpulse.event;

import me.rgunny.marketpulse.event.marketdata.infrastructure.config.kis.KISApiProperties;
import me.rgunny.marketpulse.event.marketdata.infrastructure.config.shared.StockCollectionProperties;
import me.rgunny.marketpulse.event.marketdata.infrastructure.config.PriceAlertProperties;
import me.rgunny.marketpulse.event.watchlist.infrastructure.config.WatchlistProperties;
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
@SpringBootApplication(scanBasePackages = "me.rgunny.marketpulse", exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class
})
public class EventDetectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventDetectionApplication.class, args);
    }
}
