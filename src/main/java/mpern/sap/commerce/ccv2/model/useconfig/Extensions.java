package mpern.sap.commerce.ccv2.model.useconfig;

import static mpern.sap.commerce.ccv2.model.util.ParseUtils.emptyOrSet;
import static mpern.sap.commerce.ccv2.model.util.ParseUtils.toEmpty;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Extensions {
    public static final Extensions NO_VALUE = new Extensions("", Collections.emptySet());
    public final String location;
    public final Set<String> exclude;

    private Extensions(String location, Set<String> exclude) {
        this.location = location;
        this.exclude = Collections.unmodifiableSet(exclude);
    }

    @SuppressWarnings("unchecked")
    public static Extensions fromMap(Map<String, Object> input) {
        if (input == null) {
            return NO_VALUE;
        }
        String location = toEmpty((String) input.get("location"));
        Set<String> exclude = emptyOrSet((List<String>) input.get("exclude"));
        return new Extensions(location, exclude);
    }
}
