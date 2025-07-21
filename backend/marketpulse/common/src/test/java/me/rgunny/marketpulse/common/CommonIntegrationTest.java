package me.rgunny.marketpulse.common;

import me.rgunny.marketpulse.common.error.CommonErrorCode;
import me.rgunny.marketpulse.common.exception.BusinessException;
import me.rgunny.marketpulse.common.response.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Common 모듈 통합 테스트")
class CommonIntegrationTest {

    @Test
    @DisplayName("Result 패턴을 사용한 성공 시나리오")
    void should_handle_success_scenario_with_result_pattern() {
        // Given
        String userId = "rgunny";

        // When
        Result<String> result = getUserInfo(userId);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.dataOrThrow()).isEqualTo("User: rgunny");
    }

    @Test
    @DisplayName("BusinessException 발생 시나리오")
    void should_throw_business_exception_when_invalid_input() {
        // Given
        String invalidUserId = null;

        // When & Then
        assertThatThrownBy(() -> getUserInfoWithException(invalidUserId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("입력값이 올바르지 않습니다");
    }

    // 시뮬레이션 메서드
    private Result<String> getUserInfo(String userId) {
        if (userId != null) {
            return Result.success("User: " + userId);
        }
        return Result.failure(CommonErrorCode.COMMON_VALIDATION_001);
    }

    private String getUserInfoWithException(String userId) {
        if (userId == null) {
            throw new BusinessException(CommonErrorCode.COMMON_VALIDATION_001);
        }
        return "User: " + userId;
    }
}
