package mpern.sap.commerce.build;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestUtils {

    public static void setupDependencies(Path folder) throws Exception {
        Path dbDriver = folder.resolve("jdbc-TEST.jar");
        Files.createFile(dbDriver);
        Path dummy = Paths.get(TestUtils.class.getResource("/dummy-platform.zip").toURI());
        Files.copy(dummy, folder.resolve("hybris-commerce-suite-TEST.zip"));
    }
}
