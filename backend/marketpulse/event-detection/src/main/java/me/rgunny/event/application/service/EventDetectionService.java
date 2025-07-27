package me.rgunny.event.application.service;

import me.rgunny.event.application.port.input.DetectEventUseCase;
import me.rgunny.event.application.port.output.KISCredentialPort;
import org.springframework.stereotype.Service;

@Service
public class EventDetectionService implements DetectEventUseCase {

    private final KISCredentialPort kisCredentialPort;

    public EventDetectionService(KISCredentialPort kisCredentialPort) {
        this.kisCredentialPort = kisCredentialPort;
    }

    @Override
    public void detectMarketEvents() {
        if (!kisCredentialPort.isEnabled()) {
            return;
        }

        // 이벤트 감지 로직
        System.out.println("Detecting events with KIS API: " + kisCredentialPort.getMaskedAppKey());
    }

}
