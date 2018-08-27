package mpern.sap.commerce.ccv2;

import mpern.sap.commerce.ccv2.model.Manifest;
import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;

import javax.inject.Inject;

public class CCv2Extension {

    private final DirectoryProperty generatedConfiguration;

    private final Manifest manifest;

    @Inject
    public CCv2Extension(Project project, Manifest manifest) {
        generatedConfiguration = project.getLayout().directoryProperty();
        this.manifest = manifest;
    }

    public DirectoryProperty getGeneratedConfiguration() {
        return generatedConfiguration;
    }

    public Manifest getManifest() {
        return manifest;
    }
}
