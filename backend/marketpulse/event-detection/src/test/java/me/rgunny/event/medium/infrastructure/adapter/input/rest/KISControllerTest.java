package me.rgunny.event.medium.infrastructure.adapter.input.rest;

import me.rgunny.event.application.port.input.CheckKISConnectionUseCase;
import me.rgunny.event.application.port.input.KISConnectionStatus;
import me.rgunny.event.infrastructure.adapter.input.rest.KISController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@WebFluxTest(KISController.class)
@DisplayName("KISController - /api/kis/health (medium)")
class KISControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CheckKISConnectionUseCase checkKISConnectionUseCase;

    @Test
    @DisplayName("KIS API 연결 정상 응답 시 연결 성공 상태를 반환한다")
    void givenKISApiConnected_whenCheckHealth_thenReturnsConnectedStatus() {
        // given
        KISConnectionStatus expectedStatus = new KISConnectionStatus(
                true,
                "Connection successful",
                "ABCD***EFGH",
                1250L
        );
        given(checkKISConnectionUseCase.checkConnection())
                .willReturn(Mono.just(expectedStatus));

        // when & then
        webTestClient.get()
                .uri("/api/kis/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody(KISConnectionStatus.class)
                .value(status -> {
                    assertThat(status.connected()).isTrue();
                    assertThat(status.message()).isEqualTo("Connection successful");
                    assertThat(status.maskedAppKey()).isEqualTo("ABCD***EFGH");
                    assertThat(status.responseTimeMs()).isEqualTo(1250L);
                });

        then(checkKISConnectionUseCase).should().checkConnection();
    }

    @Test
    @DisplayName("KIS API 연결 실패 시 연결 실패 상태를 반환한다")
    void givenKISApiConnectionFails_whenCheckHealth_thenReturnsFailedStatus() {
        // given
        KISConnectionStatus expectedStatus = new KISConnectionStatus(
                false,
                "Connection failed",
                "ABCD***EFGH",
                2500L
        );
        given(checkKISConnectionUseCase.checkConnection())
                .willReturn(Mono.just(expectedStatus));

        // when & then
        webTestClient.get()
                .uri("/api/kis/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody(KISConnectionStatus.class)
                .value(status -> {
                    assertThat(status.connected()).isFalse();
                    assertThat(status.message()).isEqualTo("Connection failed");
                    assertThat(status.maskedAppKey()).isEqualTo("ABCD***EFGH");
                    assertThat(status.responseTimeMs()).isEqualTo(2500L);
                });

        then(checkKISConnectionUseCase).should().checkConnection();
    }

    @Test
    @DisplayName("KIS API 비활성화 상태일 때 비활성화 상태를 반환한다")
    void givenKISApiDisabled_whenCheckHealth_thenReturnsDisabledStatus() {
        // given
        KISConnectionStatus expectedStatus = new KISConnectionStatus(
                false,
                "KIS API is disabled",
                null,
                0L
        );
        given(checkKISConnectionUseCase.checkConnection())
                .willReturn(Mono.just(expectedStatus));

        // when & then
        webTestClient.get()
                .uri("/api/kis/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody(KISConnectionStatus.class)
                .value(status -> {
                    assertThat(status.connected()).isFalse();
                    assertThat(status.message()).isEqualTo("KIS API is disabled");
                    assertThat(status.maskedAppKey()).isNull();
                    assertThat(status.responseTimeMs()).isEqualTo(0L);
                });

        then(checkKISConnectionUseCase).should().checkConnection();
    }

    @Test
    @DisplayName("UseCase에서 예외 발생 시 5xx 서버 에러를 응답한다")
    void givenUseCaseThrowsException_whenCheckHealth_thenReturnsServerError() {
        // given
        given(checkKISConnectionUseCase.checkConnection())
                .willReturn(Mono.error(new RuntimeException("UseCase error")));

        // when & then
        webTestClient.get()
                .uri("/api/kis/health")
                .exchange()
                .expectStatus().is5xxServerError();

        then(checkKISConnectionUseCase).should().checkConnection();
    }
}