package mpern.sap.commerce.ccv2.model;

import static mpern.sap.commerce.ccv2.model.util.ParseUtils.toEmpty;

import java.util.Map;

import mpern.sap.commerce.build.util.Version;

public class ExtensionPack {
    public final String name;
    public final String version;
    public final String artifact;

    public ExtensionPack(String name, String version, String artifact) {
        this.name = name;
        this.version = version;
        this.artifact = artifact;
    }

    public static ExtensionPack fromMap(Map<String, Object> jsonMap) {
        String name = toEmpty((String) jsonMap.get("name"));
        String version = toEmpty((String) jsonMap.get("version"));
        String artifact = toEmpty((String) jsonMap.get("artifact"));

        String validationVersion;
        if (artifact.isEmpty()) {
            if ((name.isEmpty() || version.isEmpty())) {
                throw new IllegalArgumentException(
                        String.format("ExtensionPack %s:%s - please specify the name and version", name, version));
            }
            validationVersion = version;
        } else {
            final String[] artifactParts = artifact.split("[:@]");
            if (artifactParts.length < 3) {
                throw new IllegalArgumentException("Invalid extensionPack.artifact string");
            }
            validationVersion = artifactParts[2];
        }
        Version.parseVersion(validationVersion);

        return new ExtensionPack(name, version, artifact);
    }
}
