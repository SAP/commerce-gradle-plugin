package mpern.sap.commerce.build;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Nested;

import mpern.sap.commerce.build.util.HybrisPlatform;
import mpern.sap.commerce.build.util.SparseBootstrap;

public abstract class HybrisPluginExtension {

    private final Property<String> version;
    private final Property<String> intExtPackVersion;
    private final Property<String> cleanGlob;

    private final ListProperty<String> bootstrapInclude;
    private final ListProperty<String> bootstrapExclude;

    private final ListProperty<Object> antTaskDependencies;

    private final MapProperty<String, Integer> previewToPatchLevel;

    private final HybrisPlatform platform;

    public HybrisPluginExtension(Project project) {
        version = project.getObjects().property(String.class);
        version.set("2105.0");

        intExtPackVersion = project.getObjects().property(String.class);
        intExtPackVersion.set("");

        cleanGlob = project.getObjects().property(String.class);

        bootstrapInclude = project.getObjects().listProperty(String.class);
        bootstrapExclude = project.getObjects().listProperty(String.class);

        antTaskDependencies = project.getObjects().listProperty(Object.class);

        previewToPatchLevel = project.getObjects().mapProperty(String.class, Integer.class);

        platform = project.getObjects().newInstance(HybrisPlatform.class, project);
    }

    public Property<String> getVersion() {
        return version;
    }

    public Property<String> getIntExtPackVersion() {
        return intExtPackVersion;
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

    public MapProperty<String, Integer> getPreviewToPatchLevel() {
        return previewToPatchLevel;
    }

    @Nested
    public abstract SparseBootstrap getSparseBootstrap();

    public void sparseBootstrap(Action<? super SparseBootstrap> action) {
        action.execute(getSparseBootstrap());
    }
}
