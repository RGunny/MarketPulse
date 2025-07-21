package me.rgunny.marketpulse.common.response;

import me.rgunny.marketpulse.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

/**
 * Sealed interface(Java 17+), Record Pattern, Switch Pattern Matching (Java 21+ Standard)
 */
public sealed interface Result<T> permits Result.Success, Result.Failure {

    record Success<T>(
            T data,
            String message,
            HttpStatus httpStatus
    ) implements Result<T> {

        public Success(T data) {
            this(data, "성공", HttpStatus.OK);
        }

        public Success(T data, String message) {
            this(data, message, HttpStatus.OK);
        }
    }

    record Failure<T>(
            ErrorCode errorCode
    ) implements Result<T> {

        public String code() {
            return errorCode.code();
        }

        public String message() {
            return errorCode.message();
        }

        public HttpStatus httpStatus() {
            return errorCode.httpStatus();
        }
    }

    // ===== Factory Methods =====

    /**
     * 성공 결과 생성
     */
    static <T> Result<T> success(T data) {
        return new Success<>(data);
    }

    static <T> Result<T> success(T data, String message) {
        return new Success<>(data, message);
    }

    static <T> Result<T> success(T data, String message, HttpStatus status) {
        return new Success<>(data, message, status);
    }

    /**
     * 실패 결과 생성
     */
    static <T> Result<T> failure(ErrorCode errorCode) {
        return new Failure<>(errorCode);
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
    default T dataOrThrow() {
        return switch (this) {
            case Success(var data, var message, var status) -> data;
            case Failure<T> failure ->
                    throw new RuntimeException(failure.message());
        };
    }

    default T dataOrElse(T defaultValue) {
        return switch (this) {
            case Success(var data, var message, var status) -> data;
            case Failure<T> failure -> defaultValue;
        };
    }

    /**
     * HTTP 상태 및 메시지 추출
     */
    default HttpStatus httpStatus() {
        return switch (this) {
            case Success(var data, var message, var status) -> status;
            case Failure<T> failure -> failure.httpStatus();
        };
    }

    default String message() {
        return switch (this) {
            case Success(var data, var message, var status) -> message;
            case Failure<T> failure -> failure.message();
        };
    }

    default String code() {
        return switch (this) {
            case Success<T> success -> "SUCCESS";
            case Failure<T> failure -> failure.code();
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