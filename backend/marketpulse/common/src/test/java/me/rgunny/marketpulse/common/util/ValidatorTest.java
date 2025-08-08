package me.rgunny.marketpulse.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ValidatorTest {

    @Nested
    @DisplayName("requireNotBlank 메서드는")
    class RequireNotBlank {

        @Test
        @DisplayName("null 또는 공백이면 예외를 던진다")
        void should_throw_when_null_or_blank() {
            // given
            String nullValue = null;
            String blankValue = "   ";

            // when & then
            assertThatThrownBy(() -> Validator.requireNotBlank(nullValue, "username"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("username must not be blank");

            assertThatThrownBy(() -> Validator.requireNotBlank(blankValue, "password"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("password must not be blank");
        }

        @Test
        @DisplayName("정상 문자열이면 그대로 반환한다")
        void should_return_value_when_valid() {
            // given
            String value = "line";

            // when
            String result = Validator.requireNotBlank(value, "nickname");

            // then
            assertThat(result).isEqualTo("line");
        }
    }

    @Nested
    @DisplayName("requireNotNull 메서드는")
    class RequireNotNull {

        @Test
        @DisplayName("null이면 예외를 던진다")
        void should_throw_when_null() {
            // given
            Object input = null;

            // when & then
            assertThatThrownBy(() -> Validator.requireNotNull(input, "target"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("target must not be null");
        }

        @Test
        @DisplayName("null이 아니면 그대로 반환한다")
        void should_return_value_when_not_null() {
            // given
            Integer input = 42;

            // when
            Integer result = Validator.requireNotNull(input, "number");

            // then
            assertThat(result).isEqualTo(42);
        }
    }

    @Nested
    @DisplayName("requireTrue 메서드는")
    class RequireTrue {

        @Test
        @DisplayName("조건이 false면 예외를 던진다")
        void should_throw_when_condition_false() {
            assertThatThrownBy(() -> Validator.requireTrue(false, "조건이 거짓입니다"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("조건이 거짓입니다");
        }

        @Test
        @DisplayName("조건이 true면 아무 일도 일어나지 않는다")
        void should_do_nothing_when_condition_true() {
            assertThatCode(() -> Validator.requireTrue(5 > 1, "잘못된 조건"))
                    .doesNotThrowAnyException();
        }
    }

}