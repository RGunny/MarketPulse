package me.rgunny.event.infrastructure.config;

import me.rgunny.event.infrastructure.adapter.output.properties.EventDetectionProperties;
import me.rgunny.event.infrastructure.adapter.output.properties.KISProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        KISProperties.class,
        EventDetectionProperties.class
})
public class EventInfraStructureConfig {

}
