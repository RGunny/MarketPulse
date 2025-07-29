package me.rgunny.event.marketdata.application.port.in;

import reactor.core.publisher.Mono;

public interface CheckKISConnectionUseCase {

    Mono<KISConnectionStatus> checkConnection();
}