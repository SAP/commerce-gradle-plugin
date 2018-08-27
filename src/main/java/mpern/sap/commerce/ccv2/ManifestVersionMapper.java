package mpern.sap.commerce.ccv2;

import java.util.HashMap;
import java.util.Map;

public class ManifestVersionMapper {
    private static Map<String, String> versionMap;
    static {
        versionMap = new HashMap<>();
        versionMap.put("18.08.0", "18.08");
    }
    public static String mapToBuildVersion(String manifestVersion) {
        return versionMap.getOrDefault(manifestVersion, manifestVersion);
    }
}
