package mpern.sap.commerce.build.util;

import java.util.Collections;

import org.gradle.api.Project;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

/**
 * Extension configuration element for HybrisPluginExtension.
 */
public abstract class SparseBootstrapSettings {

    private static final String DEFAULT_CACHE_LOCATION = ".gradle/hybris-plugin-cache";

    private final Property<Boolean> enabled;

    private final ListProperty<String> alwaysIncluded;

    private final Property<String> cacheLocation;

    public SparseBootstrapSettings(Project project) {
        enabled = project.getObjects().property(Boolean.class);
        enabled.set(false);
        alwaysIncluded = project.getObjects().listProperty(String.class);
        alwaysIncluded.set(Collections.emptyList());
        cacheLocation = project.getObjects().property(String.class);
        cacheLocation.set(DEFAULT_CACHE_LOCATION);
    }

    public Property<Boolean> getEnabled() {
        return enabled;
    }

    public ListProperty<String> getAlwaysIncluded() {
        return alwaysIncluded;
    }

    public Property<String> getCacheLocation() {
        return cacheLocation;
    }
}
