package mpern.sap.commerce.build.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;
import java.util.Properties;

import org.gradle.api.file.Directory;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

public class HybrisPlatform {
    private static final Logger LOG = Logging.getLogger(HybrisPlatform.class);

    private final Provider<Directory> platformDir;

    private final Provider<String> platformVersion;

    @javax.inject.Inject
    public HybrisPlatform(ProviderFactory providerFactory, ProjectLayout layout) {
        platformDir = providerFactory.provider(() -> layout.getProjectDirectory().dir("hybris/bin/platform"));

        platformVersion = providerFactory.provider(this::readVersion);
    }

    public Provider<Directory> getPlatformHome() {
        return platformDir;
    }

    public Provider<String> getVersion() {
        return platformVersion;
    }

    private String readVersion() {
        Directory orNull = platformDir.getOrNull();

        if (orNull == null) {
            return "NONE";
        }
        Path buildFile = orNull.file("build.number").getAsFile().toPath();
        Properties properties = new Properties();

        try (BufferedReader br = new BufferedReader(new FileReader(buildFile.toFile()))) {
            properties.load(br);
        } catch (IOException e) {
            LOG.debug("could not open build.number", e);
        }
        String bootstrappedVersion = properties.getProperty("version", "NONE");
        return bootstrappedVersion;
    }

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
