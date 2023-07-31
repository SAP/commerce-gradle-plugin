package mpern.sap.commerce.ccv2.model.util;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ParseUtils {
    public static String nullToEmpty(String v) {
        return v == null ? "" : v;
    }

    public static String validateNullOrWhitespace(String value, String message) {
        if (value == null || value.matches("\\s*")) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    public static Set<String> emptyOrSet(List<String> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptySet();
        } else {
            return new LinkedHashSet<>(list);
        }
    }

    public static List<String> emptyOrList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(list);
        }
    }

    public static String toEmpty(String input) {
        if (input == null) {
            return "";
        } else {
            return input.trim();
        }
    }

    public static boolean parseBoolean(Object input, String fieldName) {
        if (input != null) {
            if (!(input instanceof Boolean)) {
                throw new IllegalArgumentException(String.format("Field %s must be a boolean value", fieldName));
            }
            return (boolean) input;
        }
        return false;
    }

    private ParseUtils() {
    }
}
