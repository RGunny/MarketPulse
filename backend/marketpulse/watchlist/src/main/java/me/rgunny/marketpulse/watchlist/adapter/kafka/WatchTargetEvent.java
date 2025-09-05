package me.rgunny.marketpulse.watchlist.adapter.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.rgunny.marketpulse.messaging.kafka.core.KafkaEvent;
import me.rgunny.marketpulse.watchlist.domain.WatchTarget;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchTargetEvent implements KafkaEvent {

    private String id;

    private String source;

    private String type;

    private Instant time;

    private String aggregateId;

    private EventType eventType;
    private Long targetId;
    private String stockCode;
    private String stockName;
    private Integer collectInterval;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum EventType {
        CREATED, UPDATED, ACTIVATED, DEACTIVATED, DELETED
    }

    @Override
    public String getId() {
        return id != null ? id : UUID.randomUUID().toString();
    }

    @Override
    public String getSource() {
        return "marketpulse.watchlist";
    }

    @Override
    public String getType() {
        return "watchlist.target." + eventType.name().toLowerCase();
    }

    @Override
    public Instant getTime() {
        return time != null ? time : Instant.now();
    }

    @Override
    public String getAggregateId() {
        return stockCode;
    }

    public static WatchTargetEvent fromDomain(WatchTarget target, EventType eventType) {
        return WatchTargetEvent.builder()
                .id(UUID.randomUUID().toString())
                .time(Instant.now())
                .eventType(eventType)
                .targetId(target.getId())
                .stockCode(target.getStockCode())
                .stockName(target.getStockName())
                .collectInterval(target.getCollectInterval())
                .active(target.isActive())
                .createdAt(target.getCreatedAt())
                .updatedAt(target.getUpdatedAt())
                .build();
    }

}
