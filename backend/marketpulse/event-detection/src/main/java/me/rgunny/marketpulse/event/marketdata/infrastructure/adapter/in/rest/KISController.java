package me.rgunny.marketpulse.event.marketdata.infrastructure.adapter.in.rest;

import me.rgunny.marketpulse.event.marketdata.application.port.in.CheckKISConnectionUseCase;
import me.rgunny.marketpulse.event.marketdata.application.port.in.KISConnectionStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/kis")
public class KISController {

    private final CheckKISConnectionUseCase checkKISConnectionUseCase;

    public KISController(CheckKISConnectionUseCase checkKISConnectionUseCase) {
        this.checkKISConnectionUseCase = checkKISConnectionUseCase;
    }

    @GetMapping("/oauth")
    public Mono<KISConnectionStatus> validateToken() {
        return checkKISConnectionUseCase.checkConnection();
    }
}