package me.rgunny.marketpulse.event.marketdata.domain.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.rgunny.marketpulse.common.core.error.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * 종목 마스터 관련 에러 코드
 */
@Getter
@RequiredArgsConstructor
public enum StockMasterErrorCode implements ErrorCode {
    
    // 동기화 관련 오류
    SYNC_FAILED("SM001", "종목 마스터 동기화 실패", HttpStatus.INTERNAL_SERVER_ERROR),
    SYNC_ALREADY_RUNNING("SM002", "동기화가 이미 진행 중입니다", HttpStatus.CONFLICT),
    SYNC_INVALID_MODE("SM003", "유효하지 않은 동기화 모드", HttpStatus.BAD_REQUEST),
    SYNC_MARKET_NOT_FOUND("SM004", "지원하지 않는 시장입니다", HttpStatus.BAD_REQUEST),
    
    // API 연동 오류
    API_CONNECTION_FAILED("SM101", "KIS API 연결 실패", HttpStatus.SERVICE_UNAVAILABLE),
    API_AUTHENTICATION_FAILED("SM102", "KIS API 인증 실패", HttpStatus.UNAUTHORIZED),
    API_RATE_LIMIT_EXCEEDED("SM103", "API 호출 한도 초과", HttpStatus.TOO_MANY_REQUESTS),
    API_RESPONSE_ERROR("SM104", "API 응답 오류", HttpStatus.BAD_GATEWAY),
    
    // 데이터 처리 오류
    DATA_PARSING_ERROR("SM201", "데이터 파싱 오류", HttpStatus.UNPROCESSABLE_ENTITY),
    DATA_VALIDATION_ERROR("SM202", "데이터 검증 실패", HttpStatus.UNPROCESSABLE_ENTITY),
    DATA_SAVE_ERROR("SM203", "데이터 저장 실패", HttpStatus.INTERNAL_SERVER_ERROR),
    
    // 서킷브레이커 관련
    CIRCUIT_BREAKER_OPEN("SM301", "서킷브레이커 OPEN 상태", HttpStatus.SERVICE_UNAVAILABLE);
    
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
    
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