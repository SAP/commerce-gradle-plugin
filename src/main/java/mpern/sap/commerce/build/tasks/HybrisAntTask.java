package mpern.sap.commerce.build.tasks;

import static mpern.sap.commerce.build.HybrisPlugin.HYBRIS_EXTENSION;

import java.util.HashMap;
import java.util.Map;

import org.gradle.api.Task;
import org.gradle.api.execution.TaskExecutionAdapter;
import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.JavaExec;

import mpern.sap.commerce.build.HybrisPlugin;
import mpern.sap.commerce.build.HybrisPluginExtension;
import mpern.sap.commerce.build.util.HybrisPlatform;
import mpern.sap.commerce.build.util.Version;

public class HybrisAntTask extends JavaExec {
    private static final Version V_2205 = Version.parseVersion("2205.0");

    private Map<String, String> antProperties;

    private final Property<Boolean> noOp;

    public HybrisAntTask() {
        super();
        antProperties = new HashMap<>();
        noOp = getProject().getObjects().property(Boolean.class);
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
                t.systemProperty("file.encoding", "UTF-8");

                t.antProperty("maven.update.dbdrivers", "false");
                t.antProperties.forEach((k, v) -> t.args("-D" + k + "=" + v));

                final HybrisPluginExtension plugin = (HybrisPluginExtension) t.getProject().getExtensions()
                        .getByName(HYBRIS_EXTENSION);

                Version current = Version.parseVersion(plugin.getPlatform().getVersion().get());

                // ref. hybris/bin/platform/setantenv.sh in 2205
                if (current.compareTo(V_2205) >= 0) {
                    t.systemProperty("polyglot.js.nashorn-compat", "true");
                    t.systemProperty("polyglot.engine.WarnInterpreterOnly", "false");

                    t.jvmArgs("--add-exports", "java.xml/com.sun.org.apache.xpath.internal=ALL-UNNAMED");
                    t.jvmArgs("--add-exports", "java.xml/com.sun.org.apache.xpath.internal.objects=ALL-UNNAMED");
                }

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

    @Internal
    public Property<Boolean> getNoOp() {
        return noOp;
    }
}
