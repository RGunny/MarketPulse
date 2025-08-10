package me.rgunny.event.unit.application.service;

import me.rgunny.event.marketdata.application.port.out.AlertHistoryPort;
import me.rgunny.event.marketdata.application.usecase.PriceAlertService;
import me.rgunny.event.marketdata.domain.model.AlertHistory;
import me.rgunny.event.marketdata.domain.model.AlertType;
import me.rgunny.event.marketdata.domain.model.StockPrice;
import me.rgunny.event.marketdata.infrastructure.config.PriceAlertProperties;
import me.rgunny.event.notification.application.port.out.NotificationClientPort;
import me.rgunny.event.notification.application.port.out.NotificationHistoryPort;
import me.rgunny.notification.grpc.NotificationServiceProto.NotificationStatus;
import me.rgunny.notification.grpc.NotificationServiceProto.PriceAlertType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import me.rgunny.event.support.TestClockFactory;

import java.math.BigDecimal;
import java.time.Clock;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * PriceAlertService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PriceAlertService 단위 테스트")
class PriceAlertServiceTest {
    
    private PriceAlertService priceAlertService;
    
    @Mock
    private NotificationClientPort notificationClient;
    
    @Mock
    private NotificationHistoryPort notificationHistoryPort;
    
    @Mock
    private AlertHistoryPort alertHistoryPort;

    @BeforeEach
    void setUp() {
        PriceAlertProperties alertProperties = new PriceAlertProperties(
                new BigDecimal("5.0"),
                new BigDecimal("-5.0"),
                new BigDecimal("29.5"),
                new BigDecimal("-29.5"),
                30,
                60
        );
        
        // TestClockFactory 사용
        Clock fixedClock = TestClockFactory.marketMiddle();
        
        // NotificationHistoryPort mock 기본 설정 - 쿨다운 없음
        lenient().when(notificationHistoryPort.isInCooldown(anyString(), any()))
                .thenReturn(Mono.just(false));
        // save 메서드는 저장된 객체를 반환해야 함
        lenient().when(notificationHistoryPort.save(any()))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        
        // AlertHistoryPort mock 기본 설정 - 쿨다운 없음
        lenient().when(alertHistoryPort.canSendAlert(anyString(), any(AlertType.class)))
                .thenReturn(Mono.just(true));
        lenient().when(alertHistoryPort.save(any(AlertHistory.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        
        priceAlertService = new PriceAlertService(notificationClient, notificationHistoryPort, alertHistoryPort, alertProperties, fixedClock);
    }
    
    @Test
    @DisplayName("급등 알림 발송 성공")
    void givenPriceRise_whenAnalyzeAndSendAlert_thenSendsRiseAlert() {
        // given
        StockPrice stockPrice = createStockPriceWithChangeRate(new BigDecimal("6.5")); // 6.5% 상승
        
        given(notificationClient.sendPriceAlert(any(StockPrice.class), eq(PriceAlertType.RISE)))
                .willReturn(Mono.empty());
        
        // when
        Mono<Void> result = priceAlertService.analyzeAndSendAlert(stockPrice);
        
        // then
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(notificationClient).sendPriceAlert(stockPrice, PriceAlertType.RISE);
    }
    
    @Test
    @DisplayName("급락 알림 발송 성공")
    void givenPriceFall_whenAnalyzeAndSendAlert_thenSendsFallAlert() {
        // given
        StockPrice stockPrice = createStockPriceWithChangeRate(new BigDecimal("-7.2")); // 7.2% 하락
        
        given(notificationClient.sendPriceAlert(any(StockPrice.class), eq(PriceAlertType.FALL)))
                .willReturn(Mono.empty());
        
        // when
        Mono<Void> result = priceAlertService.analyzeAndSendAlert(stockPrice);
        
        // then
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(notificationClient).sendPriceAlert(stockPrice, PriceAlertType.FALL);
    }
    
    @Test
    @DisplayName("상한가 알림 발송 성공")
    void givenLimitUp_whenAnalyzeAndSendAlert_thenSendsLimitUpAlert() {
        // given
        StockPrice stockPrice = createStockPriceWithChangeRate(new BigDecimal("29.8")); // 29.8% 상승 (상한가)
        
        given(notificationClient.sendPriceAlert(any(StockPrice.class), eq(PriceAlertType.LIMIT_UP)))
                .willReturn(Mono.empty());
        
        // when
        Mono<Void> result = priceAlertService.analyzeAndSendAlert(stockPrice);
        
        // then
        StepVerifier.create(result)
                .verifyComplete();
        
        verify(notificationClient).sendPriceAlert(stockPrice, PriceAlertType.LIMIT_UP);
    }
    
    @Test
    @DisplayName("알림 임계값 미달 시 알림 발송하지 않음")
    void givenSmallChange_whenAnalyzeAndSendAlert_thenNoAlert() {
        // given
        StockPrice stockPrice = createStockPriceWithChangeRate(new BigDecimal("2.3")); // 2.3% 상승 (임계값 미달)
        
        // when
        Mono<Void> result = priceAlertService.analyzeAndSendAlert(stockPrice);
        
        // then
        StepVerifier.create(result)
                .verifyComplete();
        
        // 알림 발송하지 않음 확인
        verify(notificationClient, never()).sendPriceAlert(any(), any());
    }
    
    @Test
    @DisplayName("알림 서비스 헬스 체크 성공")
    void whenCheckNotificationServiceHealth_thenReturnsStatus() {
        // given
        given(notificationClient.checkNotificationStatus()).willReturn(Mono.just(NotificationStatus.HEALTHY.name()));
        
        // when
        Mono<String> result = priceAlertService.checkNotificationServiceHealth();
        
        // then
        StepVerifier.create(result)
                .expectNext(NotificationStatus.HEALTHY.name())
                .verifyComplete();
        
        verify(notificationClient).checkNotificationStatus();
    }
    
    private StockPrice createStockPriceWithChangeRate(BigDecimal changeRate) {
        BigDecimal previousClose = new BigDecimal("70000");
        BigDecimal change = previousClose.multiply(changeRate).divide(new BigDecimal("100"), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal currentPrice = previousClose.add(change);
        
        return StockPrice.createWithTTL(
                "005930",
                "삼성전자",
                currentPrice,
                previousClose,
                new BigDecimal("72000"),
                new BigDecimal("69000"),
                new BigDecimal("70500"),
                1500000L,
                currentPrice.add(new BigDecimal("100")), // 매도호가1
                currentPrice.subtract(new BigDecimal("100")) // 매수호가1
        );
    }
}