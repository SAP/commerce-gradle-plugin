package mpern.sap.commerce.build.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Extension configuration element for HybrisPluginExtension.
 */
public class SparseBootstrap {

    private static final String DEFAULT_CACHE_LOCATION = ".gradle/hybris-plugin-cache";

    private boolean enabled;

    private List<String> alwaysIncluded;

    private String cacheLocation;

    public SparseBootstrap() {
        enabled = false;
        alwaysIncluded = new ArrayList<>();
        cacheLocation = DEFAULT_CACHE_LOCATION;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public List<String> getAlwaysIncluded() {
        return alwaysIncluded;
    }

    public String getCacheLocation() {
        return cacheLocation;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setAlwaysIncluded(List<String> alwaysIncluded) {
        this.alwaysIncluded = alwaysIncluded;
    }

    public void setCacheLocation(String cacheLocation) {
        this.cacheLocation = cacheLocation;
    }
}
