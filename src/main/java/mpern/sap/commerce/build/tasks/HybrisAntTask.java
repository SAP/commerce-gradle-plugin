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

    public HybrisAntTask() {
        super();
    }

    public static class HybrisAntConfigureAdapter extends TaskExecutionAdapter {
        @Override
        public void beforeExecute(Task task) {
            if (task instanceof HybrisAntTask) {
                HybrisAntTask t = (HybrisAntTask) task;

                Map<String, Object> systemProperties = fetchSystemProperties();
                t.systemProperties(systemProperties);

                t.systemProperty("maven.update.dbdrivers","false");

                ConfigurableFileTree files = buildPlatformAntClasspath(t);
                t.setClasspath(files);

                HybrisPlatform platform = ((HybrisPluginExtension)t.getProject().getExtensions().getByName(HybrisPlugin.HYBRIS_EXTENSION)).getPlatform();
                t.systemProperty("ant.home", platform.getAntHome().get().getAsFile());

                t.setMain("org.apache.tools.ant.launch.Launcher");

                t.workingDir(platform.getPlatformHome());
            }
        }

        private ConfigurableFileTree buildPlatformAntClasspath(HybrisAntTask t) {
            ConfigurableFileTree files = t.getProject().fileTree("hybris/bin/platform");
            files.include("apache-ant-*/lib/ant-launcher.jar");
            return files;
        }

        private Map<String, Object> fetchSystemProperties() {
            Properties properties = System.getProperties();
            Map<String, Object> propertyMap = new HashMap<>();
            properties.forEach((key, value) -> propertyMap.put(String.valueOf(key), value));
            propertyMap.remove("user.dir");
            return propertyMap;
        }
    }
}
