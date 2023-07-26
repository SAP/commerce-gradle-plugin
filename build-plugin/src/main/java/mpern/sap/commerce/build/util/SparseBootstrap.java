package mpern.sap.commerce.build.util;

import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;

/**
 * Extension configuration element for HybrisPluginExtension.
 */
public abstract class SparseBootstrap {

    public abstract Property<Boolean> getEnabled();

    public abstract SetProperty<String> getAlwaysIncluded();
}
