package mpern.sap.commerce.ccv2.model.useconfig;

import static mpern.sap.commerce.ccv2.model.util.ParseUtils.toEmpty;

import java.util.Map;

public class Solr {
    public static final Solr NO_VALUE = new Solr("");

    public final String location;

    private Solr(String location) {
        this.location = location;
    }

    public static Solr fromMap(Map<String, Object> input) {
        if (input == null) {
            return NO_VALUE;
        }
        String location = toEmpty((String) input.get("location"));
        return new Solr(location);
    }
}
