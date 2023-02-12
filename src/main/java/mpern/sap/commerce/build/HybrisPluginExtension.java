package mpern.sap.commerce.build;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Nested;

import mpern.sap.commerce.build.util.HybrisPlatform;
import mpern.sap.commerce.build.util.SparseBootstrapSettings;

public abstract class HybrisPluginExtension {

    private final Property<String> version;
    private final Property<String> cleanGlob;

    private final ListProperty<String> bootstrapInclude;
    private final ListProperty<String> bootstrapExclude;

    private final ListProperty<Object> antTaskDependencies;

    private final HybrisPlatform platform;

    public HybrisPluginExtension(Project project) {
        version = project.getObjects().property(String.class);
        version.set("6.6.0.0");

        cleanGlob = project.getObjects().property(String.class);

        bootstrapInclude = project.getObjects().listProperty(String.class);
        bootstrapExclude = project.getObjects().listProperty(String.class);

        antTaskDependencies = project.getObjects().listProperty(Object.class);

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

    public ListProperty<Object> getAntTaskDependencies() {
        return antTaskDependencies;
    }

    @Nested
    public abstract SparseBootstrapSettings getSparseBootstrap();

    public void sparseBootstrap(Action<? super SparseBootstrapSettings> action) {
        action.execute(getSparseBootstrap());
    }
}
