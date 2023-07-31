package mpern.sap.commerce.ccv2;

import javax.inject.Inject;

import org.gradle.api.file.DirectoryProperty;

import mpern.sap.commerce.ccv2.model.Manifest;

public abstract class CCv2Extension {

    private final Manifest manifest;

    @Inject
    public CCv2Extension(Manifest manifest) {
        this.manifest = manifest;
    }

    public abstract DirectoryProperty getGeneratedConfiguration();

    public abstract DirectoryProperty getCloudExtensionPackFolder();

    public Manifest getManifest() {
        return manifest;
    }
}
