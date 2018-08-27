package mpern.sap.commerce.ccv2.model;

import mpern.sap.commerce.ccv2.model.util.ParseUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TestConfiguration {

    public static final TestConfiguration NO_VALUE = new TestConfiguration(Collections.emptySet(), Collections.emptySet(), Collections.emptySet(), Collections.emptySet());

    public final Set<String> extensions;
    public final Set<String> annotations;
    public final Set<String> packages;
    public final Set<String> excludedPackages;

    private TestConfiguration(Set<String> extensions, Set<String> annotations, Set<String> packages, Set<String> excludedPackages) {
        this.extensions = Collections.unmodifiableSet(extensions);
        this.annotations = Collections.unmodifiableSet(annotations);
        this.packages = Collections.unmodifiableSet(packages);
        this.excludedPackages = Collections.unmodifiableSet(excludedPackages);
    }

    public static TestConfiguration fromMap(Map<String, Object> jsonMap) {
        Set<String> ext = ParseUtils.emptyOrSet((List<String>) jsonMap.get("extensions"));
        Set<String> anot = ParseUtils.emptyOrSet((List<String>) jsonMap.get("annotations"));
        Set<String> pack = ParseUtils.emptyOrSet((List<String>) jsonMap.get("packages"));
        Set<String> excludedPackages = ParseUtils.emptyOrSet((List<String>) jsonMap.get("excludedPackages"));

        return new TestConfiguration(
                ext, anot, pack, excludedPackages
        );
    }

}
