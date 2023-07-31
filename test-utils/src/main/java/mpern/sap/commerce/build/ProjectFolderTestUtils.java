package mpern.sap.commerce.build;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.stream.Stream;

import mpern.sap.commerce.test.TestConstants;

public class ProjectFolderTestUtils {

    /**
     * Copies the content of a source test project folder (containing the hybris
     * folder) to a destination project folder.
     *
     * @param projectDir where to copy
     * @param template   the name of the source test project folder template
     * @throws IOException
     * @throws URISyntaxException
     */
    public static void prepareProjectFolder(Path projectDir, String template) throws IOException, URISyntaxException {
        Path sourceDir = TestConstants.testResource(template);

        // Iterate over the direct subdirectories of the source directory
        try (DirectoryStream<Path> sourceDs = Files.newDirectoryStream(sourceDir)) {
            for (Path subdir : sourceDs) {
                if (Files.isDirectory(subdir) && subdir.endsWith("hybris")) {
                    copyDirContent(projectDir, sourceDir, subdir);
                }
            }
        }
    }

    private static void copyDirContent(Path destination, Path sourceDir, Path subdir) throws IOException {
        try (Stream<Path> streamPaths = Files.walk(subdir)) {
            // Copy each subdirectory and its contents to the target directory
            streamPaths.forEach(sourcePath -> {
                Path targetPath = destination.resolve(sourceDir.relativize(sourcePath));
                if (!Files.exists(targetPath)) {
                    try {
                        Files.copy(sourcePath, targetPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private ProjectFolderTestUtils() {
        // no instances
    }
}
