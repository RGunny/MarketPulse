package me.rgunny.event.marketdata.domain.error;

import me.rgunny.marketpulse.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

public enum StockPriceErrorCode implements ErrorCode {
    
    STOCK_PRICE_ERROR("STOCK_PRICE_001", "주식 현재가 조회에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    STOCK_PRICE_REFRESH_ERROR("STOCK_PRICE_002", "주식 현재가 강제 갱신에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    STOCK_PRICE_SAVE_ERROR("STOCK_PRICE_003", "주식 현재가 저장에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    STOCK_SYMBOL_INVALID("STOCK_PRICE_004", "올바르지 않은 종목코드입니다", HttpStatus.BAD_REQUEST),
    KIS_API_ERROR("STOCK_PRICE_005", "KIS API 호출에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR);
    
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
    
    StockPriceErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
    
    @Override
    public String code() {
        return code;
    }
    
    @Override
    public String message() {
        return message;
    }
    
    @Override
    public HttpStatus httpStatus() {
        return httpStatus;
    }
}