package mpern.sap.commerce.build.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.TaskExecutionException;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.BasicFileAttributes;

public class GlobClean extends DefaultTask {
    final Property<String> glob;
    final DirectoryProperty baseFolder;

    public GlobClean() {
        glob = getProject().getObjects().property(String.class);
        baseFolder = getProject().getObjects().directoryProperty();
    }

    @TaskAction
    public void cleanup() {
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher(glob.getOrNull());
        Path path = baseFolder.get().getAsFile().toPath();
        if (!Files.exists(path)) {
            return;
        }
        try {
            Files.walkFileTree(path, new java.nio.file.SimpleFileVisitor<Path>() {
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
    public Property<String> getGlob() {
        return glob;
    }

    @SkipWhenEmpty
    @InputDirectory
    public DirectoryProperty getBaseFolder() {
        return baseFolder;
    }
}
