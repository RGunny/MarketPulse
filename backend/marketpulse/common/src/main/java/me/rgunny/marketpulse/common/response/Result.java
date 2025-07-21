package me.rgunny.marketpulse.common.response;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/**
 * Pattern Matching Style (Java 17+)
 */
public sealed interface ApiResult<T> permits ApiResult.Success, ApiResult.Failure {

    /**
     * 성공 케이스 - Record 클래스 사용
     */
    record Success<T>(
            T data,
            String message,
            HttpStatus status,
            LocalDateTime timestamp
    ) implements ApiResult<T> {

        // 간편 생성자들
        public Success(T data) {
            this(data, "성공", HttpStatus.OK, LocalDateTime.now());
        }

        public Success(T data, String message) {
            this(data, message, HttpStatus.OK, LocalDateTime.now());
        }

        public Success(T data, String message, HttpStatus status) {
            this(data, message, status, LocalDateTime.now());
        }
    }

    /**
     * 실패 케이스 - Record 클래스 사용
     */
    record Failure<T>(
            String code,
            String message,
            HttpStatus status,
            LocalDateTime timestamp
    ) implements ApiResult<T> {

        // 간편 생성자들
        public Failure(String code, String message) {
            this(code, message, HttpStatus.BAD_REQUEST, LocalDateTime.now());
        }

        public Failure(String code, String message, HttpStatus status) {
            this(code, message, status, LocalDateTime.now());
        }
    }

    // ===== Factory Methods =====

    /**
     * 성공 결과 생성
     */
    static <T> ApiResult<T> success(T data) {
        return new Success<>(data);
    }

    static <T> ApiResult<T> success(T data, String message) {
        return new Success<>(data, message);
    }

    static <T> ApiResult<T> success(T data, String message, HttpStatus status) {
        return new Success<>(data, message, status);
    }

    /**
     * 빈 성공 결과
     */
    static ApiResult<Void> success() {
        return new Success<>(null);
    }

    static ApiResult<Void> success(String message) {
        return new Success<>(null, message);
    }

    /**
     * 실패 결과 생성
     */
    static <T> ApiResult<T> failure(String code, String message) {
        return new Failure<>(code, message);
    }

    static <T> ApiResult<T> failure(String code, String message, HttpStatus status) {
        return new Failure<>(code, message, status);
    }

    // ===== Pattern Matching Helpers =====

    /**
     * 성공/실패 확인
     */
    default boolean isSuccess() {
        return this instanceof Success;
    }

    default boolean isFailure() {
        return this instanceof Failure;
    }

    /**
     * 데이터 추출 (패턴 매칭 스타일)
     */
    default T getDataOrThrow() {
        return switch (this) {
            case Success(var data, var message, var status, var timestamp) -> data;
            case Failure(var code, var message, var status, var timestamp) ->
                    throw new RuntimeException(message);
        };
    }

    default T getDataOrElse(T defaultValue) {
        return switch (this) {
            case Success(var data, var message, var status, var timestamp) -> data;
            case Failure(var code, var message, var status, var timestamp) -> defaultValue;
        };
    }

    /**
     * HTTP 상태 및 메시지 추출
     */
    default HttpStatus getStatus() {
        return switch (this) {
            case Success(var data, var message, var status, var timestamp) -> status;
            case Failure(var code, var message, var status, var timestamp) -> status;
        };
    }

    default String getMessage() {
        return switch (this) {
            case Success(var data, var message, var status, var timestamp) -> message;
            case Failure(var code, var message, var status, var timestamp) -> message;
        };
    }

    /**
     * 패턴 매칭으로 변환
     */
    default <U> U transform(
            java.util.function.Function<Success<T>, U> onSuccess,
            java.util.function.Function<Failure<T>, U> onFailure
    ) {
        return switch (this) {
            case Success<T> success -> onSuccess.apply(success);
            case Failure<T> failure -> onFailure.apply(failure);
        };
    }
}