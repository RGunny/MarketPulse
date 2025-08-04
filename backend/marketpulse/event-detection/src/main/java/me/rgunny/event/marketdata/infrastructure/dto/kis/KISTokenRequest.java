package me.rgunny.event.marketdata.infrastructure.dto.kis;

public record KISTokenRequest(
        String grant_type,
        String appkey,
        String appsecret
) {}
