package mpern.sap.commerce.build;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

import mpern.sap.commerce.build.util.HybrisPlatform;
import mpern.sap.commerce.build.util.SparseBootstrap;

public class HybrisPluginExtension {

    private final Property<String> version;
    private final Property<String> cleanGlob;

    private final ListProperty<String> bootstrapInclude;
    private final ListProperty<String> bootstrapExclude;

    private final ListProperty<Object> antTaskDependencies;

    private final SparseBootstrap sparseBootstrap;

    private final HybrisPlatform platform;

    @javax.inject.Inject
    public HybrisPluginExtension(Project project, ObjectFactory objectFactory) {
        version = project.getObjects().property(String.class);
        version.set("6.6.0.0");

        cleanGlob = project.getObjects().property(String.class);

        bootstrapInclude = project.getObjects().listProperty(String.class);
        bootstrapExclude = project.getObjects().listProperty(String.class);

        antTaskDependencies = project.getObjects().listProperty(Object.class);

        platform = project.getObjects().newInstance(HybrisPlatform.class, project);

        sparseBootstrap = objectFactory.newInstance(SparseBootstrap.class);
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

    public void sparseBootstrap(Action<? super SparseBootstrap> action) {
        action.execute(sparseBootstrap);
    }

    public SparseBootstrap getSparseBootstrap() {
        return sparseBootstrap;
    }
}
