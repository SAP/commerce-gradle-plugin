package mpern.sap.commerce.build.util;

import java.util.*;

/**
 * Extension configuration element for HybrisPluginExtension.
 */
public class SparseBootstrap {

    private boolean enabled;

    private Set<String> alwaysIncluded;

    public SparseBootstrap() {
        enabled = false;
        alwaysIncluded = new HashSet<>();
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Set<String> getAlwaysIncluded() {
        return alwaysIncluded;
    }

    public void setAlwaysIncluded(Set<String> alwaysIncluded) {
        this.alwaysIncluded = alwaysIncluded;
    }
}
