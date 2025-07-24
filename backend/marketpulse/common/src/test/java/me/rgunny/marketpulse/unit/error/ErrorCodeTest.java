package me.rgunny.marketpulse.unit.error;

import me.rgunny.marketpulse.common.error.CommonErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ErrorCode 검증 테스트 (unit)")
class ErrorCodeTest {

    @Test
    @DisplayName("공통 에러코드는 유일한 값을 가져야한다")
    void givenCommonErrorCodes_whenValidateUniqueness_thenAllCodesAreUnique() {
        // given
        Set<String> codes = new HashSet<>();

        // when & then
        for (CommonErrorCode errorCode : CommonErrorCode.values()) {
            String code = errorCode.code();
            assertThat(codes).doesNotContain(code);
            codes.add(code);
        }
    }

    @Test
    @DisplayName("에러코드 네이밍 컨벤션을 준수해야 한다")
    void givenCommonErrorCodes_whenValidateNamingConvention_thenFollowsStandard() {
        // given & when & then
        for (CommonErrorCode errorCode : CommonErrorCode.values()) {
            assertThat(errorCode.code()).startsWith("COMMON_");
            assertThat(errorCode.message()).isNotBlank();
            assertThat(errorCode.httpStatus()).isNotNull();
        }
    }
}
