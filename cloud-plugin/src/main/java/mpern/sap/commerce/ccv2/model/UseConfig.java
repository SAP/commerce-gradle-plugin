package mpern.sap.commerce.ccv2.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import mpern.sap.commerce.ccv2.model.useconfig.Extensions;
import mpern.sap.commerce.ccv2.model.useconfig.Languages;
import mpern.sap.commerce.ccv2.model.useconfig.Properties;
import mpern.sap.commerce.ccv2.model.useconfig.Solr;

public class UseConfig {
    public static final UseConfig NO_VALUE = new UseConfig(Extensions.NO_VALUE, Collections.emptyList(), Solr.NO_VALUE,
            Languages.NO_VALUE);

    public final Extensions extensions;

    public final List<Properties> properties;

    public final Solr solr;

    public final Languages languages;

    private UseConfig(Extensions extensions, List<Properties> properties, Solr solr, Languages languages) {
        this.extensions = extensions;
        this.properties = properties;
        this.solr = solr;
        this.languages = languages;
    }

    @SuppressWarnings("unchecked")
    public static UseConfig fromMap(Map<String, Object> input) {
        if (input == null) {
            return NO_VALUE;
        }
        Extensions extensions = Extensions.fromMap((Map<String, Object>) input.get("extensions"));
        List<Properties> properties = Collections.emptyList();
        List<Map<String, Object>> rawProps = (List<Map<String, Object>>) input.get("properties");
        if (rawProps != null) {
            properties = rawProps.stream().map(Properties::fromMap).filter(Objects::nonNull).toList();
        }
        Solr solr = Solr.fromMap((Map<String, Object>) input.get("solr"));
        Languages languages = Languages.fromMap((Map<String, Object>) input.get("languages"));
        return new UseConfig(extensions, properties, solr, languages);
    }

    public List<Properties> getProperties() {
        return properties;
    }
}
