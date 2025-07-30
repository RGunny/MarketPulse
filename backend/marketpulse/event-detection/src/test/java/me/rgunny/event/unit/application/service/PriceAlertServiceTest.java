package me.rgunny.event.unit.application.service;

import me.rgunny.event.marketdata.application.usecase.PriceAlertService;
import me.rgunny.event.marketdata.domain.model.StockPrice;
import me.rgunny.event.marketdata.infrastructure.config.PriceAlertProperties;
import me.rgunny.event.notification.application.port.out.NotificationClientPort;
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

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;

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
    private PriceAlertProperties alertProperties;
    
    @BeforeEach
    void setUp() {
        // PriceAlertProperties mock 설정 - lenient로 설정하여 모든 테스트에서 사용할 수 있도록 함
        lenient().when(alertProperties.getRiseThreshold()).thenReturn(new BigDecimal("5.0"));
        lenient().when(alertProperties.getFallThreshold()).thenReturn(new BigDecimal("-5.0"));
        lenient().when(alertProperties.getLimitUpThreshold()).thenReturn(new BigDecimal("29.5"));
        lenient().when(alertProperties.getLimitDownThreshold()).thenReturn(new BigDecimal("-29.5"));
        
        priceAlertService = new PriceAlertService(notificationClient, alertProperties);
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
                new BigDecimal("72000"), // 고가
                new BigDecimal("69000"), // 저가
                new BigDecimal("70500"), // 시가
                1500000L, // 거래량
                currentPrice.add(new BigDecimal("100")), // 매도호가1
                currentPrice.subtract(new BigDecimal("100")) // 매수호가1
        );
    }
}