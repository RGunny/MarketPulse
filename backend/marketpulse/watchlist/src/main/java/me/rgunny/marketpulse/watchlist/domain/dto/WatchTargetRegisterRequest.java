package me.rgunny.marketpulse.watchlist.domain.dto;

public record WatchTargetRegisterRequest(
        String stockCode,
        String stockName
){}
