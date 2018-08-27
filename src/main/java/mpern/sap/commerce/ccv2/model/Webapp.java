package mpern.sap.commerce.ccv2.model;

import java.util.Map;
import java.util.Objects;

import static mpern.sap.commerce.ccv2.model.util.ParseUtils.validateNullOrWhitespace;

public class Webapp {
    public final String name;
    public final String contextPath;

    public Webapp(String name, String contextPath) {
        this.name = name;
        this.contextPath = contextPath;
    }

    public static Webapp fromMap(Map<String, Object> jsonMap) {
        return new Webapp(
                validateNullOrWhitespace((String) jsonMap.get("name"), "Webapp.name must have a value"),
                Objects.requireNonNull((String) jsonMap.get("contextPath"), "Webapp.contextPath must be set")
        );
    }
}
