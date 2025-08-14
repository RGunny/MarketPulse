package me.rgunny.marketpulse.event.marketdata.domain.model;

/**
 * 수집 상태 정보
 */
public record CollectionStatus(
    int totalTrackedSymbols,
    int recentCollections
) {
    public static CollectionStatus of(int totalTrackedSymbols, int recentCollections) {
        return new CollectionStatus(totalTrackedSymbols, recentCollections);
    }
}