package mpern.sap.commerce.ccv2.model;

import static mpern.sap.commerce.ccv2.model.util.ParseUtils.validateNullOrWhitespace;

import java.util.*;
import java.util.stream.Collectors;

public class Aspect {

    // https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/v2011/en-US/8f494fb9617346188ddf21a971db84fc.html
    public static final String ADMIN_ASPECT = "admin";
    public static final String BACKGROUND_ASPECT = "backgroundProcessing";
    public static final Set<String> ALLOWED_ASPECTS = new HashSet<>(
            Arrays.asList("accstorefront", "backoffice", BACKGROUND_ASPECT, ADMIN_ASPECT, "api"));

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

        List<Map<String, Object>> raw = Optional.ofNullable((List<Map<String, Object>>) jsonMap.get("properties"))
                .orElse(Collections.emptyList());
        List<Property> properties = raw.stream().map(Property::fromMap).collect(Collectors.toList());

        raw = Optional.ofNullable((List<Map<String, Object>>) jsonMap.get("webapps")).orElse(Collections.emptyList());
        List<Webapp> webapps = raw.stream().map(Webapp::fromMap).collect(Collectors.toList());

        return new Aspect(name, properties, webapps);
    }
}
