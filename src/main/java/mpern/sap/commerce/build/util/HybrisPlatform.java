package mpern.sap.commerce.build.util;

import org.gradle.api.Project;
import org.gradle.api.file.Directory;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Provider;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;
import java.util.Properties;

public class HybrisPlatform {
    private static final Logger LOG = Logging.getLogger(HybrisPlatform.class);

    private final Path projectRoot;

    private final Provider<Directory> platformDir;

    private final Provider<String> platformVersion;

    private final Provider<Directory> antHome;


    @javax.inject.Inject
    public HybrisPlatform(Project project) {
        projectRoot = project.getProjectDir().toPath();
        platformDir = project.provider(() -> project.getLayout().getProjectDirectory().dir("hybris/bin/platform"));

        platformVersion = project.provider(this::readVersion);

        antHome = project.provider(() -> project.getLayout().getProjectDirectory().dir(getRelativeAntHomepath()));
    }

    public Provider<Directory> getPlatformHome() {
        return platformDir;
    }

    public Provider<String> getVersion() {
        return platformVersion;
    }

    public Provider<Directory> getAntHome() {
        return antHome;
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
//        LOG.lifecycle("found hybris platform version: {}", bootstrappedVersion);
        return bootstrappedVersion;
    }

    private String getRelativeAntHomepath() {
        try {
            AntPathVisitor visitor = new AntPathVisitor();
            Path platformPath = platformDir.get().getAsFile().toPath();
            Files.walkFileTree(platformPath, visitor);
            Path antHome = visitor
                    .getAntHome()
                    .orElseThrow(() -> new IllegalStateException("could not find hybris platform ant in hybris/bin/platform/apache-ant-*"));
            return antHome.toString();
        } catch (IOException e) {
            throw new IllegalStateException("could not find hybris platform ant", e);
        }
    }

    private static class AntPathVisitor extends SimpleFileVisitor<Path> {
        private Path foundPath;
        private final PathMatcher antPathMatcher;

        public AntPathVisitor() {
            this.foundPath = null;
            antPathMatcher = FileSystems.getDefault().getPathMatcher("glob:**/apache-ant-*");
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
