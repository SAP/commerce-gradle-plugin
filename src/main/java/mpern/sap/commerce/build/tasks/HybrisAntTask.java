package mpern.sap.commerce.build.tasks;

import mpern.sap.commerce.build.HybrisPlugin;
import mpern.sap.commerce.build.HybrisPluginExtension;
import mpern.sap.commerce.build.util.HybrisPlatform;
import org.gradle.api.Task;
import org.gradle.api.execution.TaskExecutionAdapter;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.tasks.JavaExec;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class HybrisAntTask extends JavaExec {

    private Map<String, String> antProperties;

    public HybrisAntTask() {
        super();
        antProperties = new HashMap<>();
    }

    public static class HybrisAntConfigureAdapter extends TaskExecutionAdapter {
        @Override
        public void beforeExecute(Task task) {
            if (task instanceof HybrisAntTask) {
                HybrisAntTask t = (HybrisAntTask) task;

                ConfigurableFileTree files = buildPlatformAntClasspath(t);
                t.setClasspath(files);
                t.setMain("org.apache.tools.ant.launch.Launcher");

                HybrisPlatform platform = ((HybrisPluginExtension) t.getProject().getExtensions().getByName(HybrisPlugin.HYBRIS_EXTENSION)).getPlatform();
                t.systemProperty("ant.home", platform.getAntHome().get().getAsFile());

                t.antProperty("maven.update.dbdrivers", "false");
                t.antProperties.forEach((k, v) -> t.args("-D" + k + "=" + v));

                t.workingDir(platform.getPlatformHome());
            }
        }

        private ConfigurableFileTree buildPlatformAntClasspath(HybrisAntTask t) {
            ConfigurableFileTree files = t.getProject().fileTree("hybris/bin/platform");
            files.include("apache-ant*/lib/ant-launcher.jar");
            return files;
        }
    }

    /**
     * Add a new runtime property to configure the ant target
     *
     * @param key   key of the property
     * @param value value of the property
     */
    public void antProperty(String key, String value) {
        antProperties.put(key, value);
    }

    /**
     * Set (override) all ant properties with the values of the set
     *
     * @param antProperties ant properties to use for the target
     */
    public void setAntProperties(Map<String, String> antProperties) {
        this.antProperties = new HashMap<>(antProperties);
    }
}
