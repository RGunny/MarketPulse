package me.rgunny.marketpulse.common.exception;

import me.rgunny.marketpulse.common.error.CommonErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BusinessException 테스트")
class BusinessExceptionTest {

    @Test
    @DisplayName("ErrorCode로 BusinessException을 생성할 수 있다")
    void should_create_business_exception_with_error_code() {
        // Given
        var errorCode = CommonErrorCode.COMMON_VALIDATION_001;

        // When
        BusinessException exception = new BusinessException(errorCode);

        // Then
        assertThat(exception.errorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(errorCode.message());
    }
}