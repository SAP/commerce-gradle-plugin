package mpern.sap.commerce.build;

import org.gradle.api.Project;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

import mpern.sap.commerce.build.util.HybrisPlatform;

public class HybrisPluginExtension {
    private final Property<String> version;
    private final Property<String> cleanGlob;

    private final ListProperty<String> bootstrapInclude;
    private final ListProperty<String> bootstrapExclude;

    private final HybrisPlatform platform;

    public HybrisPluginExtension(Project project) {
        version = project.getObjects().property(String.class);
        version.set("6.6.0.0");

        cleanGlob = project.getObjects().property(String.class);

        bootstrapInclude = project.getObjects().listProperty(String.class);
        bootstrapExclude = project.getObjects().listProperty(String.class);

        platform = project.getObjects().newInstance(HybrisPlatform.class, project);
    }

    public Property<String> getVersion() {
        return version;
    }

    public HybrisPlatform getPlatform() {
        return platform;
    }

    public Property<String> getCleanGlob() {
        return cleanGlob;
    }

    public ListProperty<String> getBootstrapInclude() {
        return bootstrapInclude;
    }

    public ListProperty<String> getBootstrapExclude() {
        return bootstrapExclude;
    }
}
