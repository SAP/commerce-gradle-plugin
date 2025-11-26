package mpern.sap.commerce.build;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Nested;

import mpern.sap.commerce.build.util.HybrisPlatform;
import mpern.sap.commerce.build.util.SparseBootstrap;

public abstract class HybrisPluginExtension {

    private final HybrisPlatform platform;

    @Inject
    public HybrisPluginExtension(ObjectFactory objectFactory) {
        getVersion().convention("2211");

        getIntExtPackVersion().convention("");

        platform = objectFactory.newInstance(HybrisPlatform.class);
    }

    public abstract Property<String> getVersion();

    public abstract Property<String> getIntExtPackVersion();

    public HybrisPlatform getPlatform() {
        return platform;
    }

    public abstract Property<String> getCleanGlob();

    public abstract ListProperty<String> getBootstrapInclude();

    public abstract ListProperty<String> getBootstrapExclude();

    public abstract ListProperty<Object> getAntTaskDependencies();

    public abstract MapProperty<String, Integer> getPreviewToPatchLevel();

    @Nested
    public abstract SparseBootstrap getSparseBootstrap();

    public void sparseBootstrap(Action<? super SparseBootstrap> action) {
        action.execute(getSparseBootstrap());
    }
}
