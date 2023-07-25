package mpern.sap.commerce.build.tasks;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.BasicFileAttributes;

import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;

public abstract class GlobClean extends DefaultTask {

    @TaskAction
    public void cleanup() {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher(getGlob().getOrNull());
        Path path = Path.of(getBaseFolder().get());
        if (!Files.exists(path)) {
            return;
        }
        try {
            Files.walkFileTree(path, new java.nio.file.SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (matcher.matches(file)) {
                        Files.delete(file);
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (matcher.matches(dir)) {
                        Files.delete(dir);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new TaskExecutionException(this, e);
        }
    }

    @Input
    public abstract Property<String> getGlob();

    @Input
    public abstract Property<String> getBaseFolder();
}
