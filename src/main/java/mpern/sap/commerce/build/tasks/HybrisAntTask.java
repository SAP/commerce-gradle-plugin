package mpern.sap.commerce.build.tasks;

import java.util.Map;

import org.gradle.api.Task;
import org.gradle.api.execution.TaskExecutionAdapter;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.JavaExec;

import mpern.sap.commerce.build.HybrisPlugin;
import mpern.sap.commerce.build.HybrisPluginExtension;
import mpern.sap.commerce.build.util.HybrisPlatform;

public class HybrisAntTask extends JavaExec {

    @Input
    public MapProperty<String, String> antProperties;

    @Input
    public MapProperty<String, String> fallbackAntProperties;

    private final Property<Boolean> noOp;

    public HybrisAntTask() {
        super();
        noOp = getProject().getObjects().property(Boolean.class);
        fallbackAntProperties = getProject().getObjects().mapProperty(String.class, String.class);
        antProperties = getProject().getObjects().mapProperty(String.class, String.class);
        antProperties.put("maven.update.dbdrivers", "false");
    }

    @Override
    public void exec() {
        if (noOp.getOrElse(Boolean.FALSE)) {
            return;
        }
        super.exec();
    }

    public static class HybrisAntConfigureAdapter extends TaskExecutionAdapter {
        @Override
        public void beforeExecute(Task task) {
            if (task instanceof HybrisAntTask) {
                HybrisAntTask t = (HybrisAntTask) task;

                ConfigurableFileTree files = buildPlatformAntClasspath(t);
                t.setClasspath(files);
                t.setMain("org.apache.tools.ant.launch.Launcher");

                HybrisPlatform platform = ((HybrisPluginExtension) t.getProject().getExtensions()
                        .getByName(HybrisPlugin.HYBRIS_EXTENSION)).getPlatform();
                t.systemProperty("ant.home", platform.getAntHome().get().getAsFile());

                Map<String, String> props = t.antProperties.get();

                t.fallbackAntProperties.get().forEach(props::putIfAbsent);

                props.forEach((k, v) -> t.args("-D" + k + "=" + v));

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
        this.antProperties.set(antProperties);
    }

    /**
     * Add a new runtime property to configure the ant target
     *
     * @param key   key of the property
     * @param value value of the property
     */
    public void fallbackAntProperty(String key, String value) {
        fallbackAntProperties.put(key, value);
    }

    @Internal
    public Property<Boolean> getNoOp() {
        return noOp;
    }
}
