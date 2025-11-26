package mpern.sap.commerce.ccv2.model;

import static mpern.sap.commerce.ccv2.model.util.ParseUtils.toEmpty;

import java.util.Map;

import mpern.sap.commerce.build.util.Version;

public class ExtensionPack {
    public final String name;
    public final String version;
    public final String previewVersion;
    public final String artifact;

    public ExtensionPack(String name, String version, String previewVersion, String artifact) {
        this.name = name;
        this.version = version;
        this.previewVersion = previewVersion;
        this.artifact = artifact;
    }

    public static ExtensionPack fromMap(Map<String, Object> jsonMap) {
        String name = toEmpty((String) jsonMap.get("name"));
        String version = toEmpty((String) jsonMap.get("version"));
        String previewVersion = toEmpty((String) jsonMap.get("previewVersion"));
        String artifact = toEmpty((String) jsonMap.get("artifact"));

        String validationVersion;
        if (artifact.isBlank()) {
            if (name.isBlank() || (version.isBlank() && previewVersion.isBlank())) {
                throw new IllegalArgumentException(
                        String.format("ExtensionPack %s:%s - please specify the name and version/previewVersion", name,
                                previewVersion.isBlank() ? version : previewVersion));
            }
            if (!previewVersion.isBlank() && !version.isBlank()) {
                throw new IllegalArgumentException("Either version or previewVersion, not both");
            }
            validationVersion = previewVersion.isBlank() ? version : previewVersion;
        } else {
            final String[] artifactParts = artifact.split("[:@]");
            if (artifactParts.length < 3) {
                throw new IllegalArgumentException("Invalid extensionPack.artifact string");
            }
            validationVersion = artifactParts[2];
        }
        Version.parseVersion(validationVersion);

        return new ExtensionPack(name, version, previewVersion, artifact);
    }

    public String getEffectiveVersion() {
        return previewVersion.isBlank() ? version : previewVersion;
    }

    public boolean isPreview() {
        return !this.previewVersion.isBlank();
    }
}
