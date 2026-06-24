package mpern.sap.commerce.build.tasks;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.BasicFileAttributes;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.work.DisableCachingByDefault;

@DisableCachingByDefault(because = "Cleans files in-place; no cacheable output")
public abstract class GlobClean extends DefaultTask {

    @TaskAction
    public void cleanup() {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher(getGlob().getOrNull());
        Path path = getBaseFolder().getAsFile().get().toPath();
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

    @Internal
    public abstract DirectoryProperty getBaseFolder();
}
