package me.rgunny.marketpulse.event.marketdata.application.port.in;

public record KISConnectionStatus(
        boolean connected,
        String message,
        String maskedAppKey,
        long responseTimeMs
) {}