package me.rgunny.event.infrastructure.adapter.input.rest;

import me.rgunny.event.application.port.input.CheckKISConnectionUseCase;
import me.rgunny.event.application.port.input.KISConnectionStatus;
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