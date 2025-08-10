package me.rgunny.marketpulse.common.util;

import me.rgunny.marketpulse.common.error.ErrorCode;
import me.rgunny.marketpulse.common.exception.BusinessException;

import java.util.regex.Pattern;

public class Validator {

    private Validator() {
        throw new UnsupportedOperationException("Cannot instantiate utility class");
    }

    public static String requireNotBlank(String value, ErrorCode errorCode) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(errorCode);
        }
        return value;
    }

    public static <T> T requireNotNull(T value, ErrorCode errorCode) {
        if (value == null) {
            throw new BusinessException(errorCode);
        }
        return value;
    }

    public static void requirePattern(String value, String regex, ErrorCode errorCode) {
        if (value == null || !Pattern.matches(regex, value)) {
            throw new BusinessException(errorCode);
        }
    }

    public static void requireTrue(boolean condition, String message, ErrorCode errorCode) {
        if (!condition) {
            throw new BusinessException(errorCode);
        }
    }

}
