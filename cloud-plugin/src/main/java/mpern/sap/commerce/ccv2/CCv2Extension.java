package mpern.sap.commerce.ccv2;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;

import mpern.sap.commerce.ccv2.model.Manifest;

public abstract class CCv2Extension {

    public abstract DirectoryProperty getGeneratedConfiguration();

    public abstract DirectoryProperty getCloudExtensionPackFolder();

    public abstract Property<Manifest> getManifest();
}
