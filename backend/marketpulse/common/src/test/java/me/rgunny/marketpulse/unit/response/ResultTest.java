package me.rgunny.marketpulse.unit.response;

import me.rgunny.marketpulse.common.core.error.CommonErrorCode;
import me.rgunny.marketpulse.common.core.response.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Result 패턴 매칭 테스트 (unit)")
class ResultTest {

    @Test
    @DisplayName("성공 결과 생성 시 올바른 데이터와 상태를 반환한다")
    void givenSuccessData_whenCreateSuccessResult_thenReturnsCorrectDataAndStatus() {
        // given
        String data = "test";

        // when
        Result<String> result = Result.success(data);

        // then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.dataOrThrow()).isEqualTo(data);
        assertThat(result.httpStatus()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("실패 결과 생성 시 올바른 에러코드와 상태를 반환한다")
    void givenErrorCode_whenCreateFailureResult_thenReturnsCorrectStatusAndMessage() {
        // given
        var errorCode = CommonErrorCode.COMMON_VALIDATION_001;

        // when
        Result<Void> result = Result.failure(errorCode);

        // then
        assertThat(result.isFailure()).isTrue();
        assertThat(result.code()).isEqualTo("COMMON_VALIDATION_001");
        assertThat(result.httpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @DisplayName("실패 결과에서 dataOrThrow 호출 시 RuntimeException을 발생시킨다")
    void givenFailureResult_whenCallDataOrThrow_thenThrowsRuntimeException() {
        // given
        Result<String> result = Result.failure(CommonErrorCode.COMMON_SYSTEM_001);

        // when & then
        assertThatThrownBy(result::dataOrThrow)
                .isInstanceOf(RuntimeException.class)
                .hasMessage("서버 내부 오류입니다");
    }
}
