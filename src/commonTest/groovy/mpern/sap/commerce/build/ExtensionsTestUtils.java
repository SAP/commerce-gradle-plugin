package mpern.sap.commerce.build;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static mpern.sap.commerce.build.HybrisPlugin.HYBRIS_BIN_DIR;

/**
 * Utility functions to generate extensions for the test.
 */
public final class ExtensionsTestUtils {

    public static void generateExtension(Path location, String extensionName, List<String> dependencies)
        throws IOException {

        Path extensionFolder = location.resolve(extensionName);
        Files.createDirectories(extensionFolder);
        Path extensionInfoFile = extensionFolder.resolve("extensioninfo.xml");
        Files.createFile(extensionInfoFile);

        String firstUpExtensionName = extensionName.substring(0, 1).toUpperCase(Locale.ROOT) + extensionName.substring(1);

        StringBuilder dependenciesContent = new StringBuilder();
        for (String dependency : dependencies) {
            dependenciesContent.append("<requires-extension name=\"").append(dependency).append("\"/>").append(System.lineSeparator());
        }

        String extensionInfoContentPattern =
            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>%n"
                + "  <extensioninfo xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"extensioninfo.xsd\">%n"
                + "    <extension abstractclassprefix=\"Generated\" classprefix=\"%2$s\" managername=\"%2$sManager\"%n"
                + "          managersuperclass=\"de.hybris.platform.jalo.extension.Extension\" name=\"%1$s\" usemaven=\"false\">%n"
                + "      %3$s%n"
                + "      <coremodule generated=\"true\" manager=\"de.hybris.platform.jalo.GenericManager\" packageroot=\"de.hybris.platform.%1$s\"/>%n"
                + "    </extension>%n"
                + "  </extensioninfo>";

        String extensionInfoContent = String.format(extensionInfoContentPattern,
            extensionName, firstUpExtensionName, dependenciesContent);

        Files.write(extensionInfoFile, extensionInfoContent.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static void ensureLocalExtensions(Path projectDir) throws Exception {
        Path sourceFile = Paths.get(ExtensionsTestUtils.class.getResource("/localextensions.xml").toURI());
        Path targetFile = projectDir.resolve("hybris/config/localextensions.xml");
        TestUtils.ensureParents(targetFile);
        Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
    }

    public static void removeExtension(Path projectDir, String relativePath) throws IOException {
        Path extensionPath = projectDir.resolve(HYBRIS_BIN_DIR + relativePath);
        if (Files.exists(extensionPath) && Files.isDirectory(extensionPath)) {
            removeDirContent(extensionPath.toFile());
        }
    }

    private static void removeDirContent(File dir) throws IOException {
        File[] files = dir.listFiles();

        // iterate over the files and delete them
        for (File file : files) {
            if (file.isDirectory()) {
                // call the function recursively to delete the subdirectory
                removeDirContent(file);
            } else {
                // delete the file
                file.delete();
            }
        }
        // delete the directory after deleting its contents
        dir.delete();
    }

    private ExtensionsTestUtils() {
        // no instances
    }
}
