package me.rgunny.marketpulse.common.error;

import org.springframework.http.HttpStatus;

/**
 * 공통 에러코드
 */
public enum CommonErrorCode implements ErrorCode {

    // HTTP 관련
    COMMON_HTTP_001("COMMON_HTTP_001", "잘못된 요청입니다", HttpStatus.BAD_REQUEST),
    COMMON_HTTP_002("COMMON_HTTP_002", "인증이 필요합니다", HttpStatus.UNAUTHORIZED),
    COMMON_HTTP_003("COMMON_HTTP_003", "권한이 없습니다", HttpStatus.FORBIDDEN),
    COMMON_HTTP_004("COMMON_HTTP_004", "리소스를 찾을 수 없습니다", HttpStatus.NOT_FOUND),
    COMMON_HTTP_005("COMMON_HTTP_005", "허용되지 않는 HTTP 메서드입니다", HttpStatus.METHOD_NOT_ALLOWED),

    // 검증 관련
    COMMON_VALIDATION_001("COMMON_VALIDATION_001", "입력값이 올바르지 않습니다", HttpStatus.BAD_REQUEST),
    COMMON_VALIDATION_002("COMMON_VALIDATION_002", "필수값이 누락되었습니다", HttpStatus.BAD_REQUEST),
    COMMON_VALIDATION_003("COMMON_VALIDATION_003", "입력값 형식이 올바르지 않습니다", HttpStatus.BAD_REQUEST),
    COMMON_VALIDATION_005("COMMON_VALIDATION_005", "JSON 형식이 올바르지 않습니다", HttpStatus.BAD_REQUEST),

    // 시스템 관련
    COMMON_SYSTEM_001("COMMON_SYSTEM_001", "서버 내부 오류입니다", HttpStatus.INTERNAL_SERVER_ERROR),
    COMMON_SYSTEM_002("COMMON_SYSTEM_002", "데이터베이스 오류입니다", HttpStatus.INTERNAL_SERVER_ERROR);


    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    CommonErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public String code() { return code; }

    @Override
    public String message() { return message; }

    @Override
    public HttpStatus httpStatus() { return httpStatus; }

}
