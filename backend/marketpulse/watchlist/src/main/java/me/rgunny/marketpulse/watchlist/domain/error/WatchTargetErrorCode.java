package me.rgunny.marketpulse.watchlist.domain.error;

import me.rgunny.marketpulse.common.core.error.ErrorCode;
import org.springframework.http.HttpStatus;

public enum WatchTargetErrorCode implements ErrorCode {

    WATCHTARGET_FIND_001("WATCHTARGET_FIND_001", "대상 조회에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    WATCHTARGET_STOCK_001("WATCHTARGET_STOCK_001", "중복된 종목코드입니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    WatchTargetErrorCode(String code, String message, HttpStatus httpStatus) {
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
