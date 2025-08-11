package me.rgunny.notification.domain.error;

import me.rgunny.marketpulse.common.core.error.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * 알림 도메인 에러코드
 */
public enum NotificationErrorCode implements ErrorCode {
    
    // 알림 발송 관련 에러
    NOTIFICATION_SEND_001("NOTIFICATION_SEND_001", "알림 발송에 실패했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    NOTIFICATION_SEND_002("NOTIFICATION_SEND_002", "Slack API 호출에 실패했습니다", HttpStatus.BAD_GATEWAY),
    NOTIFICATION_SEND_003("NOTIFICATION_SEND_003", "지원하지 않는 알림 채널입니다", HttpStatus.BAD_REQUEST),
    
    // 알림 검증 관련 에러
    NOTIFICATION_VALIDATION_001("NOTIFICATION_VALIDATION_001", "알림 내용이 비어있습니다", HttpStatus.BAD_REQUEST),
    NOTIFICATION_VALIDATION_002("NOTIFICATION_VALIDATION_002", "수신자 정보가 올바르지 않습니다", HttpStatus.BAD_REQUEST),
    NOTIFICATION_VALIDATION_003("NOTIFICATION_VALIDATION_003", "알림 제목이 너무 깁니다", HttpStatus.BAD_REQUEST),
    
    // gRPC 관련 에러
    NOTIFICATION_GRPC_001("NOTIFICATION_GRPC_001", "gRPC 요청 처리 중 오류가 발생했습니다", HttpStatus.INTERNAL_SERVER_ERROR),
    NOTIFICATION_GRPC_002("NOTIFICATION_GRPC_002", "잘못된 gRPC 요청 형식입니다", HttpStatus.BAD_REQUEST);
    
    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
    
    NotificationErrorCode(String code, String message, HttpStatus httpStatus) {
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