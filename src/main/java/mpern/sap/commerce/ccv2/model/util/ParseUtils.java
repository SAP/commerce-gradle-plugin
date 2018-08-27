package mpern.sap.commerce.ccv2.model.util;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ParseUtils {
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
}
