package me.rgunny.marketpulse.event.marketdata.infrastructure.dto.kis;

public record KISTokenRequest(
        String grant_type,
        String appkey,
        String appsecret
) {}
