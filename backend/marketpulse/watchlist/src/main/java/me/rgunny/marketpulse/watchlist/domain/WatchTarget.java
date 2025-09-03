package me.rgunny.marketpulse.watchlist.domain;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.rgunny.marketpulse.watchlist.domain.dto.WatchTargetRegisterRequest;
import me.rgunny.marketpulse.watchlist.domain.shared.AbstractEntity;

import java.time.LocalDateTime;

import static org.springframework.util.Assert.state;

@Entity
@Getter
@ToString
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class WatchTarget extends AbstractEntity {

    private String stockCode;           // 종목코드
    private String stockName;           // 종목명
    private int collectInterval;        // 수집 주기(초)
    private boolean active;             // 활성화 여부
    private LocalDateTime createdAt;    // 생성일시
    private LocalDateTime updatedAt;    // 수정일시

    public static WatchTarget register(WatchTargetRegisterRequest request) {
        WatchTarget watchTarget = new WatchTarget();

        watchTarget.stockCode = request.stockCode();
        watchTarget.stockName = request.stockName();
        watchTarget.collectInterval = 60;
        watchTarget.active = true;

        watchTarget.createdAt = java.time.LocalDateTime.now();
        watchTarget.updatedAt = java.time.LocalDateTime.now();

        return watchTarget;
    }

    public boolean shouldCollectNow(LocalDateTime lastCollectionTime) {
        // 비활성 상태에서는 수집하지 않음
        if (!active) {
            return false;
        }

        // 활성이면서 처음 수집인 경우
        if (lastCollectionTime == null) {
            return true;
        }

        return lastCollectionTime.plusSeconds(collectInterval).isBefore(LocalDateTime.now());
    }

    public void activate() {
        state(!active, "DEACTIVE 상태가 아닙니다");

        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        state(active, "ACTIVE 상태가 아닙니다");

        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }

}