package me.rgunny.marketpulse.watchlist.domain;

import me.rgunny.marketpulse.watchlist.domain.dto.WatchTargetRegisterRequest;

public class WatchTargetFixture {

    public static WatchTargetRegisterRequest registerWatchTargetRequest() {
        return new WatchTargetRegisterRequest("00035420060", "NAVER");
    }

    public static WatchTargetRegisterRequest registerWatchTargetRequest(String stockCode, String stockName) {
        return new WatchTargetRegisterRequest(stockCode, stockName);

    }
}
