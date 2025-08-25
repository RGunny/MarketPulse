package me.rgunny.marketpulse.common;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;

@AutoConfiguration
@ComponentScan(basePackages = {
    "me.rgunny.marketpulse.common.core",
    "me.rgunny.marketpulse.common.infrastructure",
    "me.rgunny.marketpulse.common.resilience"
})
@ConfigurationPropertiesScan(basePackages = "me.rgunny.marketpulse.common")
public class MarketPulseCommonAutoConfiguration {
}