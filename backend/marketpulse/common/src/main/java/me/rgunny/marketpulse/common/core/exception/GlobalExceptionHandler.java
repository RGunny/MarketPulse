package me.rgunny.marketpulse.common.core.exception;

import lombok.extern.slf4j.Slf4j;
import me.rgunny.marketpulse.common.core.error.CommonErrorCode;
import me.rgunny.marketpulse.common.core.response.Result;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<?>> handleBusinessException(BusinessException ex) {
        var errorCode = ex.errorCode();
        var result = Result.failure(errorCode);

        return ResponseEntity.status(result.httpStatus()).body(result);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<?>> handleValidationException(
            MethodArgumentNotValidException ex) {

        List<FieldErrorInfo> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldErrorInfo)
                .toList();

        var errorDetails = new ValidationErrorDetails(
                "입력값 검증에 실패했습니다",
                fieldErrors
        );

        var result = Result.failure(CommonErrorCode.COMMON_VALIDATION_001);

        return ResponseEntity.status(result.httpStatus()).body(result);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Result<?>> handleMissingParameter(
            MissingServletRequestParameterException ex) {

        var result = Result.failure(CommonErrorCode.COMMON_VALIDATION_002);

        return ResponseEntity.status(result.httpStatus()).body(result);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Result<?>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {

        var result = Result.failure(CommonErrorCode.COMMON_VALIDATION_003);

        return ResponseEntity.status(result.httpStatus()).body(result);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Result<?>> handleJsonParseError(
            HttpMessageNotReadableException ex) {

        var result = Result.failure(CommonErrorCode.COMMON_VALIDATION_005);

        return ResponseEntity.status(result.httpStatus()).body(result);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Result<?>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        var result = Result.failure(CommonErrorCode.COMMON_HTTP_005);

        return ResponseEntity.status(result.httpStatus()).body(result);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Result<?>> handleNoResourceFound(NoResourceFoundException ex) {
        var result = Result.failure(CommonErrorCode.COMMON_HTTP_004);

        return ResponseEntity.status(result.httpStatus()).body(result);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Result<?>> handleAccessDenied(AccessDeniedException ex) {
        var result = Result.failure(CommonErrorCode.COMMON_HTTP_003);

        return ResponseEntity.status(result.httpStatus()).body(result);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Result<?>> handleDataAccessException(DataAccessException ex) {
        var result = Result.failure(CommonErrorCode.COMMON_SYSTEM_002);

        return ResponseEntity.status(result.httpStatus()).body(result);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<?>> handleGenericException(Exception ex) {
        var result = Result.failure(CommonErrorCode.COMMON_SYSTEM_001);

        return ResponseEntity.status(result.httpStatus()).body(result);
    }

    private FieldErrorInfo toFieldErrorInfo(FieldError fieldError) {
        return new FieldErrorInfo(
                fieldError.getField(),
                fieldError.getDefaultMessage(),
                fieldError.getRejectedValue()
        );
    }

    public record ValidationErrorDetails(
            String message,
            List<FieldErrorInfo> fieldErrors
    ) {}

    public record FieldErrorInfo(
            String field,
            String message,
            Object rejectedValue
    ) {}
}
