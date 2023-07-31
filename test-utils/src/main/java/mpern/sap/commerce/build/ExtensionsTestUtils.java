package mpern.sap.commerce.build;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Locale;

import mpern.sap.commerce.test.TestConstants;

/**
 * Utility functions to generate extensions for the test.
 */
public final class ExtensionsTestUtils {

    private static final String HYBRIS_BIN_DIR = "hybris/bin/";

    public static void generateExtension(Path location, String extensionName, List<String> dependencies)
            throws IOException {

        Path extensionFolder = location.resolve(extensionName);
        Files.createDirectories(extensionFolder);
        Path extensionInfoFile = extensionFolder.resolve("extensioninfo.xml");
        Files.createFile(extensionInfoFile);

        String firstUpExtensionName = extensionName.substring(0, 1).toUpperCase(Locale.ROOT)
                + extensionName.substring(1);

        StringBuilder dependenciesContent = new StringBuilder();
        for (String dependency : dependencies) {
            dependenciesContent.append("<requires-extension name=\"").append(dependency).append("\"/>")
                    .append(System.lineSeparator());
        }

        String extensionInfoContentPattern = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>%n"
                + "  <extensioninfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"extensioninfo.xsd\">%n"
                + "    <extension abstractclassprefix=\"Generated\" classprefix=\"%2$s\" managername=\"%2$sManager\"%n"
                + "          managersuperclass=\"de.hybris.platform.jalo.extension.Extension\" name=\"%1$s\" usemaven=\"false\">%n"
                + "      %3$s%n"
                + "      <coremodule generated=\"true\" manager=\"de.hybris.platform.jalo.GenericManager\" packageroot=\"de.hybris.platform.%1$s\"/>%n"
                + "    </extension>%n" + "  </extensioninfo>";

        String extensionInfoContent = String.format(extensionInfoContentPattern, extensionName, firstUpExtensionName,
                dependenciesContent);

        Files.writeString(extensionInfoFile, extensionInfoContent, StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static void ensureLocalExtensions(Path projectDir) throws Exception {
        Path sourceFile = TestConstants.testResource("localextensions.xml");
        Path targetFile = projectDir.resolve("hybris/config/localextensions.xml");
        TestUtils.ensureParents(targetFile);
        Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
    }

    public static void removeExtension(Path projectDir, String relativePath) throws IOException {
        Path extensionPath = projectDir.resolve(HYBRIS_BIN_DIR + relativePath);
        if (Files.exists(extensionPath) && Files.isDirectory(extensionPath)) {
            Files.walkFileTree(extensionPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    // delete the file
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    // delete the directory after deleting its contents
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
    }

    private ExtensionsTestUtils() {
        // no instances
    }
}
