package mpern.sap.commerce.ccv2.model.useconfig;

import static mpern.sap.commerce.ccv2.model.util.ParseUtils.toEmpty;

import java.util.Map;

public class Properties {
    public static final Properties NO_VALUE = new Properties("", "", "");
    public final String location;
    public final String aspect;
    public final String persona;

    private Properties(String location, String aspect, String persona) {
        this.location = location;
        this.aspect = aspect;
        this.persona = persona;
    }

    public static Properties fromMap(Map<String, Object> input) {
        if (input == null) {
            return NO_VALUE;
        }
        String location = toEmpty((String) input.get("location"));
        String aspect = toEmpty((String) input.get("aspect"));
        String persona = toEmpty((String) input.get("persona"));

        return new Properties(location, aspect, persona);
    }
}
