package mpern.sap.commerce.ccv2.model.useconfig;

import static mpern.sap.commerce.ccv2.model.util.ParseUtils.toEmpty;

import java.util.Map;

public class Languages {
    public static final Languages NO_VALUE = new Languages("");
    public final String location;

    private Languages(String location) {
        this.location = location;
    }

    public static Languages fromMap(Map<String, Object> input) {
        if (input == null) {
            return NO_VALUE;
        }
        String location = toEmpty((String) input.get("location"));
        return new Languages(location);
    }
}
