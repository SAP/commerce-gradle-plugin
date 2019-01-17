package mpern.sap.commerce.ccv2.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static mpern.sap.commerce.ccv2.model.util.ParseUtils.validateNullOrWhitespace;

public class Aspect {
    public final String name;
    public final List<Property> properties;
    public final List<Webapp> webapps;

    private Aspect(String name, List<Property> properties, List<Webapp> webapps) {
        this.name = name;
        this.properties = Collections.unmodifiableList(properties);
        this.webapps = Collections.unmodifiableList(webapps);
    }

    public List<Property> getProperties() {
        return properties;
    }

    public static Aspect fromMap(Map<String, Object> jsonMap) {
        String name = validateNullOrWhitespace((String) jsonMap.get("name"), "Aspect.name must have a value");

        List<Map<String, Object>> raw = Optional.ofNullable((List<Map<String, Object>>) jsonMap.get("properties")).orElse(Collections.emptyList());
        List<Property> properties = raw.stream().map(Property::fromMap).collect(Collectors.toList());

        raw = Optional.ofNullable((List<Map<String, Object>>) jsonMap.get("webapps")).orElse(Collections.emptyList());
        List<Webapp> webapps = raw.stream().map(Webapp::fromMap).collect(Collectors.toList());

        return new Aspect(name, properties, webapps);
    }
}
