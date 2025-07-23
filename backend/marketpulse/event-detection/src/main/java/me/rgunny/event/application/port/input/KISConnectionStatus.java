package me.rgunny.event.application.port.input;

public record KISConnectionStatus(
        boolean connected,
        String message,
        String maskedAppKey,
        long responseTimeMs
) {}
