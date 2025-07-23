package me.rgunny.event.application.port.output;

import reactor.core.publisher.Mono;

public interface KISApiPort {
    Mono<String> getAccessToken();
    Mono<Boolean> validateConnection();
}
