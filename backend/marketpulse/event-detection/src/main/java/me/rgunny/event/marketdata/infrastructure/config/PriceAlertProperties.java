package me.rgunny.event.marketdata.infrastructure.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

/**
 * 가격 알림 임계값 설정
 */
@ConfigurationProperties(prefix = "marketpulse.alert.price")
@Validated
public record PriceAlertProperties(

    @NotNull
    BigDecimal riseThreshold,

    @NotNull
    BigDecimal fallThreshold,

    @NotNull
    BigDecimal limitUpThreshold,

    @NotNull
    BigDecimal limitDownThreshold,

    @DefaultValue("30")
    Integer cooldownMinutes,

    @DefaultValue("60")
    Integer limitCooldownMinutes

) {}