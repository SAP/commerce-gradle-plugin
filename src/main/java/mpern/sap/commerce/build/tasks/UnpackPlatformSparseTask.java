package mpern.sap.commerce.build.tasks;

import java.util.HashSet;
import java.util.Set;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

/**
 * Task implementation to unpack the platform in sparse mode.
 */
public class UnpackPlatformSparseTask extends DefaultTask {

    @TaskAction
    public void unpack() {
        getLogger().lifecycle("Unpack platform sparse action");
        getLogger().warn("Unpack platform sparse action");
    }

}
