package mpern.sap.commerce.build.tasks;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import javax.inject.Inject;

import org.gradle.api.file.ConfigurableFileTree;
import org.gradle.api.model.ObjectFactory;
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
    private final HybrisPluginExtension extension;
    private final ObjectFactory objectFactory;

    @Inject
    public HybrisAntTask(ObjectFactory objectFactory) {
        super();
        this.extension = ((HybrisPluginExtension) getProject().getExtensions()
                .getByName(HybrisPlugin.HYBRIS_EXTENSION));
        this.objectFactory = objectFactory;

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

        HybrisPlatform platform = extension.getPlatform();

        systemProperty("ant.home", getRelativeAntHomepath(platform.getPlatformHome().get().getAsFile().toPath()));
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

    private String getRelativeAntHomepath(Path platformPath) {
        try {
            AntPathVisitor visitor = new AntPathVisitor();
            Files.walkFileTree(platformPath, visitor);
            Path antHome = visitor.getAntHome().orElseThrow(() -> new IllegalStateException(
                    "could not find hybris platform ant in hybris/bin/platform/apache-ant*"));
            return antHome.toString();
        } catch (IOException e) {
            throw new IllegalStateException("could not find hybris platform ant", e);
        }
    }

    private ConfigurableFileTree buildPlatformAntClasspath() {
        ConfigurableFileTree files = objectFactory.fileTree().from("hybris/bin/platform");
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

    private static class AntPathVisitor extends SimpleFileVisitor<Path> {
        private Path foundPath;
        private final PathMatcher antPathMatcher;

        public AntPathVisitor() {
            this.foundPath = null;
            antPathMatcher = FileSystems.getDefault().getPathMatcher("glob:**/apache-ant*");
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if (antPathMatcher.matches(dir)) {
                foundPath = dir;
                return FileVisitResult.TERMINATE;
            }
            return FileVisitResult.CONTINUE;
        }

        public Optional<Path> getAntHome() {
            return Optional.ofNullable(foundPath);
        }
    }
}
