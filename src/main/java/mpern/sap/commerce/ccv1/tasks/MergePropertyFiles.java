package mpern.sap.commerce.ccv1.tasks;

import mpern.sap.commerce.ccv1.util.PropertyFileMerger;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.WriteProperties;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MergePropertyFiles extends WriteProperties {
    private final ConfigurableFileCollection inputFiles;

    @InputFiles
    public ConfigurableFileCollection getInputFiles() {
        return inputFiles;
    }

    public MergePropertyFiles() {
        this.inputFiles = getProject().files();
    }

    @Override
    public Map<String, String> getProperties() {

        List<Path> propertyFiles = new ArrayList<>();
        inputFiles.forEach(f -> propertyFiles.add(f.toPath()));


        Map<String, String> finalProperties;

        finalProperties = new HashMap<>(new PropertyFileMerger(propertyFiles).mergeProperties());


        Map<String, String> configuredProperties = super.getProperties();
        finalProperties.putAll(configuredProperties);

        return finalProperties;
    }
}
