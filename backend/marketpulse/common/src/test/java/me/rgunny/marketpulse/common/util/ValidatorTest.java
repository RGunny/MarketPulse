package me.rgunny.marketpulse.common.util;

import me.rgunny.marketpulse.common.error.ErrorCode;
import me.rgunny.marketpulse.common.exception.BusinessException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ValidatorTest {

    enum DummyErrorCode implements ErrorCode {
        FIELD_REQUIRED("E001", "Field is required"),
        INVALID_FORMAT("E002", "Invalid format"),
        VALUE_REQUIRED("E003", "Value must not be null"),
        ASSERTION_FAILED("E004", "Assertion failed");

        private final String code;
        private final String message;

        DummyErrorCode(String code, String message) {
            this.code = code;
            this.message = message;
        }

        @Override public String code() { return code; }
        @Override public String message() { return message; }
        @Override public org.springframework.http.HttpStatus httpStatus() {
            return org.springframework.http.HttpStatus.BAD_REQUEST;
        }
    }

    @Nested
    @DisplayName("requireNotBlank 메서드는")
    class RequireNotBlankTest {

        @Test
        @DisplayName("null 또는 공백일 경우 예외를 던진다")
        void should_throw_when_blank() {
            Assertions.assertThatThrownBy(() ->
                            Validator.requireNotBlank("   ", "field", DummyErrorCode.FIELD_REQUIRED))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Field is required");

            Assertions.assertThatThrownBy(() ->
                            Validator.requireNotBlank(null, "field", DummyErrorCode.FIELD_REQUIRED))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Field is required");
        }

        @Test
        @DisplayName("정상 입력이면 그대로 반환한다")
        void should_return_value_when_valid() {
            String input = "valid";
            String result = Validator.requireNotBlank(input, "field", DummyErrorCode.FIELD_REQUIRED);

            Assertions.assertThat(result).isEqualTo(input);
        }
    }

    @Nested
    @DisplayName("requireNotNull 메서드는")
    class RequireNotNullTest {

        @Test
        @DisplayName("null이면 예외를 던진다")
        void should_throw_when_null() {
            Assertions.assertThatThrownBy(() ->
                            Validator.requireNotNull(null, "field", DummyErrorCode.VALUE_REQUIRED))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Value must not be null");
        }

        @Test
        @DisplayName("null이 아니면 그대로 반환한다")
        void should_return_value_when_not_null() {
            Integer input = 42;
            Integer result = Validator.requireNotNull(input, "field", DummyErrorCode.VALUE_REQUIRED);

            Assertions.assertThat(result).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("requirePattern 메서드는")
    class RequirePatternTest {

        @Test
        @DisplayName("패턴이 맞지 않으면 예외를 던진다")
        void should_throw_when_pattern_does_not_match() {
            Assertions.assertThatThrownBy(() ->
                            Validator.requirePattern("abc", "\\d{6}", "symbol", DummyErrorCode.INVALID_FORMAT))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Invalid format");
        }

        @Test
        @DisplayName("패턴이 맞으면 예외가 발생하지 않는다")
        void should_pass_when_pattern_matches() {
            Assertions.assertThatCode(() ->
                            Validator.requirePattern("123456", "\\d{6}", "symbol", DummyErrorCode.INVALID_FORMAT))
                    .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("requireTrue 메서드는")
    class RequireTrueTest {

        @Test
        @DisplayName("조건이 false면 예외를 던진다")
        void should_throw_when_condition_false() {
            Assertions.assertThatThrownBy(() ->
                            Validator.requireTrue(false, "조건이 거짓입니다", DummyErrorCode.ASSERTION_FAILED))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Assertion failed");
        }

        @Test
        @DisplayName("조건이 true면 예외를 던지지 않는다")
        void should_not_throw_when_condition_true() {
            Assertions.assertThatCode(() ->
                            Validator.requireTrue(true, "조건이 거짓입니다", DummyErrorCode.ASSERTION_FAILED))
                    .doesNotThrowAnyException();
        }
    }

}