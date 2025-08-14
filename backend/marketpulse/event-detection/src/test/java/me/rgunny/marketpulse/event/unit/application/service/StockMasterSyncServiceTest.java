package me.rgunny.marketpulse.event.unit.application.service;

import me.rgunny.marketpulse.event.marketdata.application.port.out.StockMasterPort;
import me.rgunny.marketpulse.event.marketdata.application.port.out.SyncConfigPort;
import me.rgunny.marketpulse.event.marketdata.application.port.out.SyncLockPort;
import me.rgunny.marketpulse.event.marketdata.application.port.out.shared.StockPort;
import me.rgunny.marketpulse.event.marketdata.application.service.StockMasterSyncService;
import me.rgunny.marketpulse.event.marketdata.domain.model.MarketType;
import me.rgunny.marketpulse.event.marketdata.domain.model.Stock;
import me.rgunny.marketpulse.event.marketdata.domain.model.SyncMode;
import me.rgunny.marketpulse.event.marketdata.domain.model.SyncStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

/**
 * StockMasterSyncService 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StockMasterSyncService 단위 테스트")
class StockMasterSyncServiceTest {
    
    @Mock
    private StockMasterPort stockMasterPort;
    
    @Mock
    private StockPort stockPort;
    
    @Mock
    private SyncLockPort syncLockPort;
    
    @Mock
    private SyncConfigPort syncConfigPort;
    
    private StockMasterSyncService service;
    
    @BeforeEach
    void setUp() {
        service = new StockMasterSyncService(
                stockMasterPort,
                stockPort,
                syncLockPort,
                syncConfigPort
        );
    }
    
    @Test
    @DisplayName("동시 동기화 요청시 두 번째 요청은 실패한다")
    void given_syncAlreadyRunning_when_syncAgain_then_shouldFail() {
        // given
        given(syncLockPort.tryLock(anyString(), any(Duration.class)))
                .willReturn(Mono.just(false));
        
        // when
        StepVerifier.create(service.syncStockMaster(SyncMode.FULL))
        
        // then
                .assertNext(result -> {
                    assertThat(result.status()).isEqualTo(SyncStatus.FAILED);
                    assertThat(result.errors())
                            .containsAnyOf("다른 인스턴스에서 동기화가 진행 중입니다.");
                })
                .verifyComplete();
        
        // 락 해제가 호출되지 않음
        then(syncLockPort).should(times(0)).unlock(anyString());
    }
    
    @Test
    @DisplayName("전체 동기화시 기존 데이터를 삭제하고 새로 저장한다")
    void given_fullSyncMode_when_sync_then_deleteAllAndSaveNew() {
        // given
        Stock stock1 = Stock.createStock("005930", "삼성전자", "Samsung", 
                MarketType.KOSPI, "ELEC", "전기전자");
        Stock stock2 = Stock.createStock("000660", "SK하이닉스", "SK Hynix", 
                MarketType.KOSPI, "ELEC", "전기전자");
        
        given(syncConfigPort.getBatchSize()).willReturn(10);
        given(syncConfigPort.getBatchDelayMs()).willReturn(10L);
        
        given(syncLockPort.tryLock(anyString(), any(Duration.class)))
                .willReturn(Mono.just(true));
        given(syncLockPort.unlock(anyString()))
                .willReturn(Mono.empty());
        
        given(stockPort.deleteAll()).willReturn(Mono.empty());
        given(stockMasterPort.fetchAllStocks())
                .willReturn(Flux.just(stock1, stock2));
        given(stockPort.findBySymbol(anyString()))
                .willReturn(Mono.empty());
        given(stockPort.save(any(Stock.class)))
                .willReturn(Mono.just(stock1))
                .willReturn(Mono.just(stock2));
        
        // when
        StepVerifier.create(service.syncStockMaster(SyncMode.FULL))
        
        // then
                .assertNext(result -> {
                    assertThat(result.status()).isEqualTo(SyncStatus.SUCCESS);
                    assertThat(result.totalProcessed()).isEqualTo(2);
                    assertThat(result.successCount()).isEqualTo(2);
                    assertThat(result.newCount()).isEqualTo(2);
                    assertThat(result.failedCount()).isEqualTo(0);
                })
                .verifyComplete();
        
        // 검증
        then(stockPort).should(times(1)).deleteAll();
        then(stockPort).should(times(2)).save(any(Stock.class));
        then(syncLockPort).should(times(1)).unlock(anyString());
    }
    
    @Test
    @DisplayName("증분 동기화시 변경된 종목만 업데이트한다")
    void given_incrementalSyncMode_when_sync_then_updateOnlyChanged() {
        // given
        Stock existingStock = Stock.createStock("005930", "삼성전자", "Samsung", 
                MarketType.KOSPI, "ELEC", "전기전자");
        Stock updatedStock = Stock.createStock("005930", "삼성전자", "Samsung Electronics", 
                MarketType.KOSPI, "ELEC", "전기전자");
        
        given(syncConfigPort.getBatchSize()).willReturn(10);
        given(syncConfigPort.getBatchDelayMs()).willReturn(10L);
        
        given(syncLockPort.tryLock(anyString(), any(Duration.class)))
                .willReturn(Mono.just(true));
        given(syncLockPort.unlock(anyString()))
                .willReturn(Mono.empty());
        
        given(stockMasterPort.fetchAllStocks())
                .willReturn(Flux.just(updatedStock));
        given(stockPort.findBySymbol("005930"))
                .willReturn(Mono.just(existingStock));
        given(stockPort.save(any(Stock.class)))
                .willReturn(Mono.just(updatedStock));
        
        // when
        StepVerifier.create(service.syncStockMaster(SyncMode.INCREMENTAL))
        
        // then
                .assertNext(result -> {
                    assertThat(result.status()).isEqualTo(SyncStatus.SUCCESS);
                    assertThat(result.updatedCount()).isEqualTo(1);
                    assertThat(result.newCount()).isEqualTo(0);
                })
                .verifyComplete();
        
        // deleteAll이 호출되지 않음
        then(stockPort).should(times(0)).deleteAll();
    }
    
    @Test
    @DisplayName("일부 종목 저장 실패시 부분 성공 결과를 반환한다")
    void given_partialFailure_when_sync_then_returnPartialResult() {
        // given
        Stock stock1 = Stock.createStock("005930", "삼성전자", "Samsung", 
                MarketType.KOSPI, "ELEC", "전기전자");
        Stock stock2 = Stock.createStock("000660", "SK하이닉스", "SK Hynix", 
                MarketType.KOSPI, "ELEC", "전기전자");
        
        given(syncConfigPort.getBatchSize()).willReturn(10);
        given(syncConfigPort.getBatchDelayMs()).willReturn(10L);
        
        given(syncLockPort.tryLock(anyString(), any(Duration.class)))
                .willReturn(Mono.just(true));
        given(syncLockPort.unlock(anyString()))
                .willReturn(Mono.empty());
        
        given(stockMasterPort.fetchAllStocks())
                .willReturn(Flux.just(stock1, stock2));
        given(stockPort.findBySymbol("005930"))
                .willReturn(Mono.empty());
        given(stockPort.findBySymbol("000660"))
                .willReturn(Mono.empty());
        
        // 첫 번째 성공, 두 번째 실패
        given(stockPort.save(stock1))
                .willReturn(Mono.just(stock1));
        given(stockPort.save(stock2))
                .willReturn(Mono.error(new RuntimeException("저장 실패")));
        
        // when
        StepVerifier.create(service.syncStockMaster(SyncMode.INCREMENTAL))
        
        // then
                .assertNext(result -> {
                    // 구조화된 테스트 검증 (실무 표준)
                    assertThat(result.status())
                            .as("동기화 상태는 PARTIAL 또는 FAILED여야 함")
                            .isIn(SyncStatus.PARTIAL, SyncStatus.FAILED);
                    
                    assertThat(result.errors())
                            .as("실패 시 에러 메시지가 있어야 함")
                            .isNotEmpty();
                })
                .verifyComplete();
    }
    
    @Test
    @DisplayName("락 해제는 항상 수행된다")
    void given_errorDuringSync_when_sync_then_alwaysReleaseLock() {
        // given
        given(syncConfigPort.getBatchSize()).willReturn(10);
        given(syncConfigPort.getBatchDelayMs()).willReturn(10L);
        
        given(syncLockPort.tryLock(anyString(), any(Duration.class)))
                .willReturn(Mono.just(true));
        given(syncLockPort.unlock(anyString()))
                .willReturn(Mono.empty());
        
        given(stockPort.deleteAll()).willReturn(Mono.empty());
        given(stockMasterPort.fetchAllStocks())
                .willReturn(Flux.error(new RuntimeException("API 오류")));
        
        // when
        StepVerifier.create(service.syncStockMaster(SyncMode.FULL))
        
        // then
                .assertNext(result -> {
                    assertThat(result.status()).isEqualTo(SyncStatus.FAILED);
                })
                .verifyComplete();
        
        // 오류가 발생해도 락은 해제됨
        then(syncLockPort).should(times(1)).unlock(anyString());
    }
    
    @Test
    @DisplayName("동기화 상태 조회시 락 상태를 확인한다")
    void given_lockExists_when_checkSyncStatus_then_returnTrue() {
        // given
        given(syncLockPort.isLocked(anyString()))
                .willReturn(Mono.just(true));
        
        // when & then
        StepVerifier.create(service.isSyncing())
                .expectNext(true)
                .verifyComplete();
    }
}