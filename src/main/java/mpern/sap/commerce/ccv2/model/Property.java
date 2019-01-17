package mpern.sap.commerce.ccv2.model;

import java.util.Map;

import static mpern.sap.commerce.ccv2.model.util.ParseUtils.nullToEmpty;
import static mpern.sap.commerce.ccv2.model.util.ParseUtils.validateNullOrWhitespace;

public class Property {
    public final String key;
    public final String value;
    public final String persona;
    public final boolean secret;

    private Property(String key, String value, String persona, boolean secret) {
        this.key = key;
        this.value = value;
        this.persona = persona;
        this.secret = secret;
    }

    public static Property fromMap(Map<String, Object> jsonMap) {
        return new Property(
                validateNullOrWhitespace((String) jsonMap.get("key"), "Property.key must have a value"),
                nullToEmpty((String) jsonMap.get("value")),
                nullToEmpty((String) jsonMap.get("persona")),
                jsonMap.get("secret") != null && (boolean) jsonMap.get("secret")
        );
    }
}
