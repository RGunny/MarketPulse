package me.rgunny.marketpulse.common.error;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ErrorCode 검증 테스트")
class ErrorCodeTest {

    @Test
    @DisplayName("공통 에러코드는 유일한 값을 가져야한다")
    void should_have_unique_error_code_values() {
        // Given
        Set<String> codes = new HashSet<>();

        // When & Then
        for (CommonErrorCode errorCode : CommonErrorCode.values()) {
            String code = errorCode.code();
            assertThat(codes).doesNotContain(code);
            codes.add(code);
        }
    }

    @Test
    @DisplayName("에러코드 네이밍 컨벤션을 준수해야 한다")
    void should_use_COMMON_prefix_and_valid_message() {
        // When & Then
        for (CommonErrorCode errorCode : CommonErrorCode.values()) {
            assertThat(errorCode.code()).startsWith("COMMON_");
            assertThat(errorCode.message()).isNotBlank();
            assertThat(errorCode.httpStatus()).isNotNull();
        }
    }
}
