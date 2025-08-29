package me.rgunny.marketpulse.watchlist.domain.dto;

import me.rgunny.marketpulse.watchlist.domain.WatchCategory;

public record WatchTargetCreateRequest (
        String stockCode,
        String stockName,
        WatchCategory category
){}
