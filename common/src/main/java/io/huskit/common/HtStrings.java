package io.huskit.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Formatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HtStrings {

    private static final Formatter formatter = new Formatter();

    public static String doubleQuoteIfNotAlready(String value) {
        if (value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {
            return value;
        }
        return "\"" + value + "\"";
    }

    public static String doubleQuote(String value) {
        return "\"" + value + "\"";
    }

    public static String doubleQuotedParam(Object key, Object value) {
        return "\"" + key.toString() + "=" + value.toString() + "\"";
    }

    public static String doubleQuotedParam(Object key, Object value1, Object value2) {
        return "\"" + key.toString() + "=" + value1.toString() + "=" + value2.toString() + "\"";
    }

    public static String format(String format, Object... args) {
        return String.format(format, args);
    }
}
