package mpern.sap.commerce.ccv2.validation.impl;

import static mpern.sap.commerce.ccv2.model.Aspect.BACKGROUND_ASPECT;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import groovy.lang.Tuple2;

import mpern.sap.commerce.ccv2.model.Manifest;
import mpern.sap.commerce.ccv2.validation.*;
import mpern.sap.commerce.ccv2.validation.Error;

public class CloudHotfolderValidator implements Validator {
    public static final String HOTFOLDER_EXTENSION = "azurecloudhotfolder";

    private final ExtensionsResolver resolver;
    private final Path projectRoot;

    public CloudHotfolderValidator(Path projectRoot, ExtensionsResolver resolver) {
        this.projectRoot = projectRoot;
        this.resolver = resolver;
    }

    @Override
    public List<Error> validate(Manifest manifest) throws Exception {
        Tuple2<Set<String>, List<String>> listing = resolver.listAllConfiguredExtensions(manifest);
        if (listing.getFirst().contains(HOTFOLDER_EXTENSION)) {
            Map<String, String> effectiveProperties = new HashMap<>();
            manifest.aspects.stream().filter(a -> BACKGROUND_ASPECT.equals(a.name)).flatMap(a -> a.properties.stream())
                    .forEach(p -> effectiveProperties.put(p.key, p.value));

            manifest.useConfig.properties.stream().filter(p -> BACKGROUND_ASPECT.equals(p.aspect))
                    .map(p -> ValidationUtils.validateAndNormalizePath(this.projectRoot, "", p.location))
                    .filter(p -> p.getFirst() != null).map(Tuple2::getFirst).forEach(p -> {
                        try (InputStream stream = Files.newInputStream(p)) {
                            Properties props = new Properties();
                            props.load(stream);
                            props.forEach((k, v) -> effectiveProperties.put((String) k, (String) v));
                        } catch (IOException e) {
                            // ignore
                        }
                    });
            String nodeGroups = effectiveProperties.get("cluster.node.groups");
            if (nodeGroups == null || !nodeGroups.contains("integration")
                    || !nodeGroups.contains("yHotfolderCandidate")) {
                return Collections.singletonList(new Error.Builder().setLevel(Level.WARNING)
                        .setLocation("aspects[?name == '%s']", BACKGROUND_ASPECT)
                        .setMessage(
                                "Cloud hotfolders enabled (extension `azurecloudhotfolder`), but `backgroundProcessing` nodes are not configured to process the imports (via property `cluster.node.groups`).\nPlease updated your properties according to the documentation.")
                        .setCode("W-003").createError());
            }
        }
        return Collections.emptyList();
    }
}
