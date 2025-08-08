package me.rgunny.marketpulse.common.util;

import io.micrometer.common.util.StringUtils;
import me.rgunny.marketpulse.common.error.ErrorCode;
import me.rgunny.marketpulse.common.exception.BusinessException;

import java.util.regex.Pattern;

public class Validator {

    private Validator() {
        throw new UnsupportedOperationException("Cannot instantiate utility class");
    }

    public static String requireNotBlank(String value, String fieldName, ErrorCode errorCode) {
        if (StringUtils.isBlank(value)) {
            throw new BusinessException(errorCode);
        }
        return value;
    }

    public static <T> T requireNotNull(T value, String fieldName, ErrorCode errorCode) {
        if (value == null) {
            throw new BusinessException(errorCode);
        }
        return value;
    }

    public static void requirePattern(String value, String regex, String fieldName, ErrorCode errorCode) {
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
