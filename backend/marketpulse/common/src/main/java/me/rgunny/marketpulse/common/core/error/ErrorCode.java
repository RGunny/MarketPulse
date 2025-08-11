package me.rgunny.marketpulse.common.core.error;

import org.springframework.http.HttpStatus;

/**
 * 에러코드 기본 인터페이스
 */
public interface ErrorCode {

    String code();
    String message();
    HttpStatus httpStatus();
}
