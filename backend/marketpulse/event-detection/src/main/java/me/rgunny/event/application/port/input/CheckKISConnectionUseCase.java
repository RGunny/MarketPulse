package me.rgunny.event.application.port.input;

import reactor.core.publisher.Mono;

public interface CheckKISConnectionUseCase {

    Mono<KISConnectionStatus> checkConnection();
}
