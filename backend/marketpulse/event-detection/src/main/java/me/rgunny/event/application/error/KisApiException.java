package me.rgunny.event.application.error;

import me.rgunny.marketpulse.common.exception.BusinessException;

/**
 * KIS API 관련 예외
 */
public class KisApiException extends BusinessException {
    
    private final String symbol;
    
    public KisApiException(String symbol) {
        super(StockPriceErrorCode.KIS_API_ERROR);
        this.symbol = symbol;
    }
    
    public String getSymbol() {
        return symbol;
    }
}