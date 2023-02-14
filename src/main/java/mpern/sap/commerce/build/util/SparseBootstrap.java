package mpern.sap.commerce.build.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Extension configuration element for HybrisPluginExtension.
 */
public class SparseBootstrap {

    private boolean enabled;

    private List<String> alwaysIncluded;

    public SparseBootstrap() {
        enabled = false;
        alwaysIncluded = new ArrayList<>();
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getAlwaysIncluded() {
        return alwaysIncluded;
    }

    public void setAlwaysIncluded(List<String> alwaysIncluded) {
        this.alwaysIncluded = alwaysIncluded;
    }
}
