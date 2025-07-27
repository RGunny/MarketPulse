package me.rgunny.event.infrastructure.config;

import me.rgunny.event.infrastructure.config.EventDetectionProperties;
import me.rgunny.event.infrastructure.config.KISApiProperties;
import me.rgunny.event.infrastructure.config.StockCollectionProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 인프라 계층 설정 관리
 * 모든 @ConfigurationProperties 클래스를 중앙에서 관리
 */
@Configuration
@EnableConfigurationProperties({
        EventDetectionProperties.class,
        KISApiProperties.class,
        StockCollectionProperties.class
})
public class EventInfraStructureConfig {

}
