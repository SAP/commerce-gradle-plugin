package mpern.sap.commerce.build;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

public class TestUtils {

    public static void setupDependencies(Path destination) throws Exception {
        Path dbDriver = destination.resolve("jdbc-TEST.jar");
        Files.createFile(dbDriver);


        generateDummyPlatform(destination, "TEST");
    }

    public static void generateDummyPlatform(Path destination, String version) throws IOException, URISyntaxException {
        Path targetZip = destination.resolve(String.format("hybris-commerce-suite-%s.zip", version));
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        try (FileSystem zipfs = FileSystems.newFileSystem(URI.create("jar:" + targetZip.toUri().toString()), env, null)) {
            Path sourceDir = Paths.get(TestUtils.class.getResource("/dummy-platform").toURI());
            Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path relative = sourceDir.relativize(file);
                    if (relative.getParent() != null) {
                        Path path = zipfs.getPath(relative.getParent().toString());
                        Files.createDirectories(path);
                    }
                    Path target = zipfs.getPath(relative.toString());
                    Files.copy(file, target);
                    return super.visitFile(file, attrs);
                }
            });
        }
    }
}
