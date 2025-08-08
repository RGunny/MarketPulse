package me.rgunny.marketpulse.common.util;

public class Validator {

    private Validator() {
        throw new UnsupportedOperationException("Cannot instantiate utility class");
    }

    public static String requireNotBlank(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(fieldName + " must not be blank");
        }
        return value;
    }

    public static <T> T requireNotNull(T obj, String fieldName) {
        if (obj == null) {
            throw new IllegalStateException(fieldName + " must not be null");
        }
        return obj;
    }

    public static void requireTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

}
