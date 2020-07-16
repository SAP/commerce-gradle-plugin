package mpern.sap.commerce.ccv2.model;

import java.util.Map;

import static mpern.sap.commerce.ccv2.model.util.ParseUtils.toEmpty;

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

        if (!name.isEmpty() && version.isEmpty()) {
            throw new IllegalArgumentException(String.format("ExtensionPack %s: please specify the version", name));
        }
        return new ExtensionPack(name, version, artifact);
    }
}
