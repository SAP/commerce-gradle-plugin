package mpern.sap.commerce.ccv1.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class PropertyFileMerger {

    private final List<Path> propertyFiles;

    public PropertyFileMerger(List<Path> propertyFiles) {
        this.propertyFiles = propertyFiles;
    }

    public Map<String, String> mergeProperties() {
        Map<String, String> finalProperties = new HashMap<>();
        for (Path propertyFile : propertyFiles) {
            if (!Files.exists(propertyFile)) {
                continue;
            }
            try (BufferedReader reader = Files.newBufferedReader(propertyFile)) {
                Properties properties = new Properties();
                properties.load(reader);
                properties.forEach((key, value) -> finalProperties.put(String.valueOf(key), String.valueOf(value)));
            } catch (IOException e) {
                //ignore
            }
        }
        return Collections.unmodifiableMap(finalProperties);
    }
}
