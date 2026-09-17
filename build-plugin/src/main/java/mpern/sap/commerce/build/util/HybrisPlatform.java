package mpern.sap.commerce.build.util;

import javax.inject.Inject;

import org.gradle.api.file.Directory;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

public class HybrisPlatform {

    private final Provider<Directory> platformDir;

    private Provider<String> platformVersion;

    @Inject
    public HybrisPlatform(ProviderFactory providerFactory, ProjectLayout layout) {
        platformDir = providerFactory.provider(() -> layout.getProjectDirectory().dir("hybris/bin/platform"));
        platformVersion = providerFactory.provider(() -> "NONE");
    }

    public void setVersionService(Provider<PlatformVersionService> versionService) {
        platformVersion = versionService.map(PlatformVersionService::getVersion);
    }

    public Provider<Directory> getPlatformHome() {
        return platformDir;
    }

    public Provider<String> getVersion() {
        return platformVersion;
    }
}
