package me.rgunny.event.marketdata.domain.error;

import me.rgunny.marketpulse.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * 종목 관련 에러 코드
 */
public enum StockErrorCode implements ErrorCode {
    
    // 400 Bad Request - 잘못된 요청
    STOCK_SYMBOL_REQUIRED("STOCK_001", "종목코드는 필수입니다", HttpStatus.BAD_REQUEST),
    STOCK_NAME_REQUIRED("STOCK_002", "종목명은 필수입니다", HttpStatus.BAD_REQUEST),
    STOCK_MARKET_TYPE_REQUIRED("STOCK_003", "시장구분은 필수입니다", HttpStatus.BAD_REQUEST),
    STOCK_INVALID_SYMBOL_FORMAT("STOCK_004", "잘못된 종목코드 형식입니다", HttpStatus.BAD_REQUEST),
    
    // 404 Not Found - 리소스 없음
    STOCK_NOT_FOUND("STOCK_101", "종목을 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    STOCK_MASTER_NOT_FOUND("STOCK_102", "종목 마스터 정보를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    
    // 409 Conflict - 충돌
    STOCK_ALREADY_EXISTS("STOCK_201", "이미 존재하는 종목코드입니다", HttpStatus.CONFLICT),
    STOCK_DUPLICATE_SYMBOL("STOCK_202", "중복된 종목코드입니다", HttpStatus.CONFLICT),
    
    // 500 Internal Server Error - 서버 오류
    STOCK_SAVE_FAILED("STOCK_301", "종목 정보 저장에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    STOCK_UPDATE_FAILED("STOCK_302", "종목 정보 수정에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    STOCK_DELETE_FAILED("STOCK_303", "종목 정보 삭제에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    STOCK_CACHE_ERROR("STOCK_304", "종목 캐시 처리 중 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR);
    
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
    
    StockErrorCode(String code, String message, HttpStatus httpStatus) {
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