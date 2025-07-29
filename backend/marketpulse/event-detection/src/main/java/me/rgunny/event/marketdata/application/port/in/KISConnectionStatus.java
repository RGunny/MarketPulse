package me.rgunny.event.marketdata.application.port.in;

public record KISConnectionStatus(
        boolean connected,
        String message,
        String maskedAppKey,
        long responseTimeMs
) {}