package me.rgunny.event.integration;

import me.rgunny.event.marketdata.application.usecase.PriceAlertService;
import me.rgunny.event.marketdata.domain.model.StockPrice;
import me.rgunny.notification.grpc.NotificationServiceProto.NotificationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

/**
 * Notification 서비스와의 실제 gRPC 통신 테스트
 * 
 * 1. notification 서비스가 실행 중이어야 함
 * 2. local --> intellij run configuration:
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Notification Integration 테스트")
class NotificationIntegrationTest {
    
    @Autowired
    private PriceAlertService priceAlertService;
    
    @BeforeEach
    void setUp() {
        // notification 서비스 헬스 체크 후 테스트 진행
        StepVerifier.create(priceAlertService.checkNotificationServiceHealth())
                .expectNext(NotificationStatus.HEALTHY.name())
                .verifyComplete();
    }
    
    @Test
    @DisplayName("실제 gRPC 통신으로 급등 알림 발송")
    void givenRealService_whenSendRiseAlert_thenSuccess() {
        // given
        StockPrice stockPrice = createStockPriceWithChangeRate(new BigDecimal("7.0"));
        
        // when & then
        StepVerifier.create(priceAlertService.analyzeAndSendAlert(stockPrice))
                .verifyComplete();
    }
    
    @Test
    @DisplayName("실제 gRPC 통신으로 급락 알림 발송")  
    void givenRealService_whenSendFallAlert_thenSuccess() {
        // given
        StockPrice stockPrice = createStockPriceWithChangeRate(new BigDecimal("-6.0"));
        
        // when & then
        StepVerifier.create(priceAlertService.analyzeAndSendAlert(stockPrice))
                .verifyComplete();
    }
    
    @Test
    @DisplayName("Notification 서비스 헬스 체크")
    void whenCheckHealth_thenReturnsHealthy() {
        // when & then
        StepVerifier.create(priceAlertService.checkNotificationServiceHealth())
                .expectNext(NotificationStatus.HEALTHY.name())
                .verifyComplete();
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
                currentPrice.add(new BigDecimal("100")),
                currentPrice.subtract(new BigDecimal("100"))
        );
    }
}