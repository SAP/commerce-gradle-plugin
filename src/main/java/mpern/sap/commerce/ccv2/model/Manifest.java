package mpern.sap.commerce.ccv2.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static mpern.sap.commerce.ccv2.model.util.ParseUtils.emptyOrSet;
import static mpern.sap.commerce.ccv2.model.util.ParseUtils.validateNullOrWhitespace;

public class Manifest {
    public final String commerceSuiteVersion;
    public final Set<String> extensions;

    public final List<Addon> storefrontAddons;

    public final List<Property> properties;

    public final List<Aspect> aspects;

    public final TestConfiguration tests;

    public final TestConfiguration webTests;

    public Manifest(String commerceSuiteVersion, Set<String> extensions, List<Addon> storefrontAddons, List<Property> properties, List<Aspect> aspects, TestConfiguration tests, TestConfiguration webTests) {
        this.commerceSuiteVersion = commerceSuiteVersion;
        this.extensions = Collections.unmodifiableSet(extensions);
        this.storefrontAddons = Collections.unmodifiableList(storefrontAddons);
        this.properties = Collections.unmodifiableList(properties);
        this.aspects = Collections.unmodifiableList(aspects);
        this.tests = tests;
        this.webTests = webTests;
    }

    public static Manifest fromMap(Map<String, Object> jsonMap) {
        String version = validateNullOrWhitespace((String) jsonMap.get("commerceSuiteVersion"), "Manifest.commerceSuiteVersion must have a value");
        Set<String> extensions = emptyOrSet((List<String>) jsonMap.get("extensions"));
        if (extensions.isEmpty()) {
            throw new IllegalArgumentException("Manifest.extensions must have values");
        }

        List<Map<String, Object>> raw = (List<Map<String, Object>>) jsonMap.get("storefrontAddons");
        List<Addon> addons;
        if (raw == null) {
            addons = Collections.emptyList();
        } else {
            addons = raw.stream().map(Addon::fromMap).collect(Collectors.toList());
        }

        raw = (List<Map<String, Object>>) jsonMap.get("properties");
        List<Property> properties;
        if (raw == null) {
            properties = Collections.emptyList();
        } else {
            properties = raw.stream().map(Property::fromMap).collect(Collectors.toList());
        }

        raw = (List<Map<String, Object>>) jsonMap.get("aspects");
        List<Aspect> aspects;
        if (raw == null) {
            aspects = Collections.emptyList();
        } else {
            aspects = raw.stream().map(Aspect::fromMap).collect(Collectors.toList());
        }

        Map<String, Object> rawConfig = (Map<String, Object>) jsonMap.get("tests");
        TestConfiguration tests = Optional.ofNullable(rawConfig).map(TestConfiguration::fromMap).orElse(TestConfiguration.NO_VALUE);

        rawConfig = (Map<String, Object>) jsonMap.get("webTests");
        TestConfiguration webTests = Optional.ofNullable(rawConfig).map(TestConfiguration::fromMap).orElse(TestConfiguration.NO_VALUE);

        return new Manifest(
                version,
                extensions,
                addons,
                properties,
                aspects,
                tests,
                webTests
        );
    }

    public List<Property> getProperties() {
        return properties;
    }
}
