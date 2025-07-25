package me.rgunny.event.medium.infrastructure.adapter.output;

import me.rgunny.event.application.port.output.KISCredentialPort;
import me.rgunny.event.application.port.output.KISTokenCachePort;
import me.rgunny.event.infrastructure.adapter.output.KISApiAdapter;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * KISApiAdapter 통합 테스트 (Medium Test)
 * 
 * MockWebServer를 사용하여 실제 HTTP 통신을 시뮬레이션합니다.
 * 이는 실무에서 외부 API 연동을 테스트하는 표준 방법입니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("KISApiAdapter - HTTP 통합 테스트 (medium)")
class KISApiAdapterMediumTest {

    private MockWebServer mockWebServer;
    private KISApiAdapter kisApiAdapter;
    
    @Mock private KISCredentialPort credentialPort;
    @Mock private KISTokenCachePort tokenCachePort;
    
    private static final String TEST_APP_KEY = "test-app-key";
    private static final String TEST_APP_SECRET = "test-app-secret";
    private static final String TEST_TOKEN = "test-access-token";

    @BeforeEach
    void setUp() throws IOException {
        // MockWebServer 시작
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        
        // WebClient를 MockWebServer로 향하도록 설정
        String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());
        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
        
        // Adapter 생성
        kisApiAdapter = new KISApiAdapter(webClient, credentialPort, tokenCachePort);
        
        // 기본 Mock 설정
        given(credentialPort.getDecryptedAppKey()).willReturn(TEST_APP_KEY);
        given(credentialPort.getDecryptedAppSecret()).willReturn(TEST_APP_SECRET);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Nested
    @DisplayName("OAuth 토큰 발급")
    class OAuthTokenTests {

        @Test
        @DisplayName("정상적인 토큰 발급 요청과 응답을 처리한다")
        void givenValidRequest_whenGetAccessToken_thenReturnsToken() throws InterruptedException {
            // given
            String responseBody = """
                {
                    "access_token": "%s",
                    "token_type": "Bearer",
                    "expires_in": 86400
                }
                """.formatted(TEST_TOKEN);
            
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody(responseBody));

            // when
            Mono<String> result = kisApiAdapter.getAccessToken();

            // then
            StepVerifier.create(result)
                    .expectNext(TEST_TOKEN)
                    .verifyComplete();

            // 요청 검증
            RecordedRequest request = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
            assertThat(request).isNotNull();
            assertThat(request.getMethod()).isEqualTo("POST");
            assertThat(request.getPath()).isEqualTo("/oauth2/tokenP");
            assertThat(request.getHeader("Content-Type")).isEqualTo("application/json");
            
            // 요청 본문 검증
            String requestBody = request.getBody().readUtf8();
            assertThat(requestBody).contains(TEST_APP_KEY);
            assertThat(requestBody).contains(TEST_APP_SECRET);
            assertThat(requestBody).contains("client_credentials");
        }

        @Test
        @DisplayName("401 에러 시 적절한 예외를 발생시킨다")
        void givenUnauthorized_whenGetAccessToken_thenThrowsException() {
            // given
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(401)
                    .setHeader("Content-Type", "application/json")
                    .setBody("{\"error\":\"invalid_client\"}"));

            // when
            Mono<String> result = kisApiAdapter.getAccessToken();

            // then
            StepVerifier.create(result)
                    .expectErrorMatches(throwable -> 
                            throwable.getMessage().contains("401"))
                    .verify();
        }

        @Test
        @DisplayName("네트워크 타임아웃을 적절히 처리한다")
        void givenTimeout_whenGetAccessToken_thenHandlesTimeout() {
            // given
            // 실무에서는 실제 타임아웃을 기다리지 않고 빠르게 실패하도록 설정
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setBodyDelay(100, TimeUnit.MILLISECONDS) // 짧은 지연으로 변경
                    .setBody("{}"));
            
            // 테스트용 짧은 타임아웃 설정
            WebClient webClient = WebClient.builder()
                    .baseUrl(String.format("http://localhost:%s", mockWebServer.getPort()))
                    .build();
            
            // 테스트용 짧은 타임아웃을 가진 adapter 생성
            KISApiAdapter testAdapter = new KISApiAdapter(webClient, credentialPort, tokenCachePort) {
                @Override
                public Mono<String> getAccessToken() {
                    // 테스트를 위해 50ms 타임아웃 적용
                    return super.getAccessToken().timeout(Duration.ofMillis(50));
                }
            };

            // when
            Mono<String> result = testAdapter.getAccessToken();

            // then
            StepVerifier.create(result)
                    .expectError()
                    .verify();
        }
    }

    @Nested
    @DisplayName("토큰 캐싱 통합 플로우")
    class TokenCachingFlowTests {

        @Test
        @DisplayName("캐시 미스 시 API 호출 후 캐시에 저장한다")
        void givenCacheMiss_whenGetCachedOrNewToken_thenFetchesAndCaches() throws InterruptedException {
            // given
            given(tokenCachePort.getToken()).willReturn(Mono.empty());
            given(tokenCachePort.saveToken(eq(TEST_TOKEN), any(Duration.class)))
                    .willReturn(Mono.empty());

            String responseBody = """
                {
                    "access_token": "%s",
                    "token_type": "Bearer",
                    "expires_in": 86400
                }
                """.formatted(TEST_TOKEN);
            
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody(responseBody));

            // when
            Mono<String> result = kisApiAdapter.getCachedOrNewToken();

            // then
            StepVerifier.create(result)
                    .expectNext(TEST_TOKEN)
                    .verifyComplete();

            // API 호출 확인
            RecordedRequest request = mockWebServer.takeRequest(5, TimeUnit.SECONDS);
            assertThat(request).isNotNull();
            assertThat(request.getPath()).isEqualTo("/oauth2/tokenP");

            // 캐시 저장 확인
            then(tokenCachePort).should().saveToken(eq(TEST_TOKEN), any(Duration.class));
        }

        @Test
        @DisplayName("연속 호출 시 캐시를 활용하여 API 호출을 최소화한다")
        void givenMultipleCalls_whenGetCachedOrNewToken_thenUsesCache() {
            // given
            // 첫 번째 호출: 캐시 미스
            given(tokenCachePort.getToken())
                    .willReturn(Mono.empty())
                    .willReturn(Mono.just(TEST_TOKEN)); // 두 번째 호출: 캐시 히트
            
            given(tokenCachePort.saveToken(eq(TEST_TOKEN), any(Duration.class)))
                    .willReturn(Mono.empty());

            String responseBody = """
                {
                    "access_token": "%s",
                    "token_type": "Bearer",
                    "expires_in": 86400
                }
                """.formatted(TEST_TOKEN);
            
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setHeader("Content-Type", "application/json")
                    .setBody(responseBody));

            // when - 두 번 호출
            Mono<String> firstCall = kisApiAdapter.getCachedOrNewToken();
            Mono<String> secondCall = kisApiAdapter.getCachedOrNewToken();

            // then
            StepVerifier.create(firstCall)
                    .expectNext(TEST_TOKEN)
                    .verifyComplete();

            StepVerifier.create(secondCall)
                    .expectNext(TEST_TOKEN)
                    .verifyComplete();

            // API는 한 번만 호출됨
            assertThat(mockWebServer.getRequestCount()).isEqualTo(1);
        }
    }
}
