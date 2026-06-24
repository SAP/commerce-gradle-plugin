package mpern.sap.commerce.build.util;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import javax.inject.Inject;

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

    @Inject
    public HybrisPlatform(ProviderFactory providerFactory, ProjectLayout layout) {
        platformDir = providerFactory.provider(() -> layout.getProjectDirectory().dir("hybris/bin/platform"));
        Provider<String> buildNumberFileContents = providerFactory
                .fileContents(platformDir.map(dir -> dir.file("build.number"))).getAsText();
        platformVersion = buildNumberFileContents.map(HybrisPlatform::extractVersion).orElse("NONE");
    }

    public Provider<Directory> getPlatformHome() {
        return platformDir;
    }

    public Provider<String> getVersion() {
        return platformVersion;
    }

    private static String extractVersion(String fileContents) {
        Properties properties = new Properties();
        try (StringReader reader = new StringReader(fileContents)) {
            properties.load(reader);
        } catch (IOException e) {
            LOG.debug("could not parse build.number", e);
        }
        return properties.getProperty("version", "NONE");
    }
}
