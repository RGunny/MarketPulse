package me.rgunny.marketpulse.unit.exception;

import me.rgunny.marketpulse.common.core.error.CommonErrorCode;
import me.rgunny.marketpulse.common.core.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BusinessException 테스트 (unit)")
class BusinessExceptionTest {

    @Test
    @DisplayName("ErrorCode로 BusinessException을 생성할 수 있다")
    void givenErrorCode_whenCreateBusinessException_thenExceptionCreatedWithCorrectProperties() {
        // given
        var errorCode = CommonErrorCode.COMMON_VALIDATION_001;

        // when
        BusinessException exception = new BusinessException(errorCode);

        // then
        assertThat(exception.errorCode()).isEqualTo(errorCode);
        assertThat(exception.getMessage()).isEqualTo(errorCode.message());
    }
}
