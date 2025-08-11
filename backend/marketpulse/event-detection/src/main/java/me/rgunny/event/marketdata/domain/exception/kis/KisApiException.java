package me.rgunny.event.marketdata.domain.exception.kis;

import me.rgunny.marketpulse.common.core.exception.BusinessException;
import me.rgunny.event.marketdata.domain.error.StockPriceErrorCode;

/**
 * KIS API 관련 예외
 */
public class KisApiException extends BusinessException {
    
    private final String symbol;
    private final String detail;
    
    public KisApiException(String symbol) {
        super(StockPriceErrorCode.STOCK_PRICE_005);
        this.symbol = symbol;
        this.detail = null;
    }
    
    public KisApiException(StockPriceErrorCode errorCode, String detail) {
        super(errorCode);
        this.symbol = null;
        this.detail = detail;
    }
    
    public String getSymbol() {
        return symbol;
    }
    
    public String getDetail() {
        return detail;
    }
}