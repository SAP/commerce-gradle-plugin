package mpern.sap.commerce.ccv2;

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;

import mpern.sap.commerce.ccv2.model.Manifest;

public class CCv2Extension {

    private final DirectoryProperty generatedConfiguration;
    private final DirectoryProperty cloudExtensionPackFolder;

    private final Manifest manifest;

    @Inject
    public CCv2Extension(Project project, Manifest manifest) {
        generatedConfiguration = project.getObjects().directoryProperty();
        cloudExtensionPackFolder = project.getObjects().directoryProperty();
        this.manifest = manifest;
    }

    public DirectoryProperty getGeneratedConfiguration() {
        return generatedConfiguration;
    }

    public DirectoryProperty getCloudExtensionPackFolder() {
        return cloudExtensionPackFolder;
    }

    public Manifest getManifest() {
        return manifest;
    }
}
