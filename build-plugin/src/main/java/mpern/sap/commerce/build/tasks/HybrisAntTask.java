package mpern.sap.commerce.build.tasks;

import java.util.*;

import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.options.Option;

import mpern.sap.commerce.build.HybrisPlugin;
import mpern.sap.commerce.build.HybrisPluginExtension;
import mpern.sap.commerce.build.util.HybrisPlatform;
import mpern.sap.commerce.build.util.Version;

public abstract class HybrisAntTask extends JavaExec {
    private static final Version V_2205 = Version.parseVersion("2205.0");

    private List<String> fromCommandLine = Collections.emptyList();

    public HybrisAntTask() {
        super();
        getAntProperties().put("maven.update.dbdrivers", "false");
        getMainClass().set("org.apache.tools.ant.launch.Launcher");
    }

    @Override
    public void exec() {
        if (getNoOp().getOrElse(Boolean.FALSE)) {
            return;
        }

        ConfigurableFileTree files = buildPlatformAntClasspath();
        setClasspath(files);

        HybrisPlatform platform = ((HybrisPluginExtension) getProject().getExtensions()
                .getByName(HybrisPlugin.HYBRIS_EXTENSION)).getPlatform();

        systemProperty("ant.home", platform.getAntHome().get().getAsFile());
        systemProperty("file.encoding", "UTF-8");

        Map<String, String> props = new LinkedHashMap<>(getAntProperties().get());

        getFallbackAntProperties().get().forEach(props::putIfAbsent);

        // @formatter:off
        fromCommandLine.stream()
                .map(s -> s.split("=", 2))
                .peek(split -> {
                    if (split.length < 2) {
                        getLogger().warn("Malformed antProperty; must be in the format 'key=value' (actual: {})", split[0]);
                    }
                })
                .filter(split -> split.length > 1)
                .forEach(split -> props.put(split[0], split[1]));
        // @formatter:on

        props.forEach((k, v) -> args("-D" + k + "=" + v));

        Version current = Version.parseVersion(platform.getVersion().get());

        // ref. hybris/bin/platform/setantenv.sh in 2205
        if (current.compareTo(V_2205) >= 0) {
            systemProperty("polyglot.js.nashorn-compat", "true");
            systemProperty("polyglot.engine.WarnInterpreterOnly", "false");

            jvmArgs("--add-exports", "java.xml/com.sun.org.apache.xpath.internal=ALL-UNNAMED");
            jvmArgs("--add-exports", "java.xml/com.sun.org.apache.xpath.internal.objects=ALL-UNNAMED");
        }

        workingDir(platform.getPlatformHome());

        super.exec();
    }

    private ConfigurableFileTree buildPlatformAntClasspath() {
        ConfigurableFileTree files = getProject().fileTree("hybris/bin/platform");
        files.include("apache-ant*/lib/ant-launcher.jar");
        return files;
    }

    /**
     * Add a new runtime property to configure the ant target
     *
     * @param key   key of the property
     * @param value value of the property
     */
    public void antProperty(String key, String value) {
        getAntProperties().put(key, value);
    }

    /**
     * Add a new runtime property to configure the ant target
     *
     * @param key   key of the property
     * @param value value of the property
     */
    public void fallbackAntProperty(String key, String value) {
        getFallbackAntProperties().put(key, value);
    }

    @Option(option = "antProperty", description = "Additional properties for Commerce ant targets")
    public void setFromCommandLine(List<String> values) {
        this.fromCommandLine = values;
    }

    @Internal
    public abstract Property<Boolean> getNoOp();

    @Input
    public abstract MapProperty<String, String> getAntProperties();

    @Input
    public abstract MapProperty<String, String> getFallbackAntProperties();
}
