package me.rgunny.event.marketdata.infrastructure.util;

import java.math.BigDecimal;
import java.util.Optional;

public class KISFieldParser {

    /**
     * String to BigDecimal 변환
     */
    public static BigDecimal toBigDecimal(String str) {
        return Optional.ofNullable(str)
                .filter(s -> !s.isBlank())
                .map(BigDecimal::new)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * String to Long 변환
     */
    public static Long toLong(String str) {
        return Optional.ofNullable(str)
                .filter(s -> !s.isBlank())
                .map(Long::parseLong)
                .orElse(0L);
    }

    /**
     * String to Integer 변환
     */
    public static Integer toInteger(String str) {
        return Optional.ofNullable(str)
                .filter(s -> !s.isBlank())
                .map(Integer::parseInt)
                .orElse(0);
    }
}
