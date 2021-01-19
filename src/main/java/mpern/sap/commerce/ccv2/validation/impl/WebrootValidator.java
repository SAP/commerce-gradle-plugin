package mpern.sap.commerce.ccv2.validation.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import groovy.lang.Tuple2;

import mpern.sap.commerce.ccv2.model.Aspect;
import mpern.sap.commerce.ccv2.model.Manifest;
import mpern.sap.commerce.ccv2.model.Property;
import mpern.sap.commerce.ccv2.model.useconfig.Properties;
import mpern.sap.commerce.ccv2.validation.Error;
import mpern.sap.commerce.ccv2.validation.ValidationUtils;
import mpern.sap.commerce.ccv2.validation.Validator;

public class WebrootValidator implements Validator {
    // shared/global - manifest
    // shared/global - file

    // aspect - webroots
    // aspect - properties
    // aspect - files

    // persona - props
    // persona - files
    // persona - aspect - props
    // persona - aspect - files

    private final Path projectRoot;

    public WebrootValidator(Path projectRoot) {
        this.projectRoot = projectRoot;
    }

    @Override
    public List<Error> validate(Manifest manifest) throws Exception {
        Map<String, Set<String>> occurences = new HashMap<>();
        for (int i = 0; i < manifest.properties.size(); i++) {
            Property p = manifest.properties.get(i);
            checkProperty(p.key, String.format("properties[%d]", i), occurences);
        }
        for (int i = 0; i < manifest.useConfig.properties.size(); i++) {
            Properties properties = manifest.useConfig.properties.get(i);
            Tuple2<Path, List<Error>> result = ValidationUtils.validateAndNormalizePath(this.projectRoot, "",
                    properties.location);
            if (result.getFirst() != null) {
                try (InputStream stream = Files.newInputStream(result.getFirst())) {
                    java.util.Properties props = new java.util.Properties();
                    props.load(stream);
                    int finalI = i;
                    props.forEach((k, v) -> checkProperty((String) k,
                            String.format("useConfig.properties[%d].location (%s)", finalI, properties.location),
                            occurences));
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        for (Aspect aspect : manifest.aspects) {
            for (int i = 0; i < aspect.properties.size(); i++) {
                Property p = aspect.properties.get(i);
                checkProperty(p.key, String.format("useConfig.aspects[?name == '%s'].properties[%d]", aspect.name, i),
                        occurences);
            }

        }
        if (occurences.isEmpty()) {
            return Collections.emptyList();
        } else {
            StringBuilder message = new StringBuilder("Do not configure webroots in properties.\nFaulty properties:\n");
            String location = null;
            for (Map.Entry<String, Set<String>> errorEntry : occurences.entrySet()) {
                message.append("-`").append(errorEntry.getKey()).append("` @\n");
                for (String s : errorEntry.getValue()) {
                    if (location == null) {
                        location = s;
                    } else {
                        location = "<multiple>";
                    }
                    message.append("    ").append(s).append("\n");
                }
            }
            return Collections.singletonList(new Error.Builder().setLocation(location).setCode("E-017")
                    .setMessage(message.toString()).createError());
        }
    }

    private void checkProperty(String key, String location, Map<String, Set<String>> occurences) {
        key = key.trim();
        if (key.endsWith(".webroot")) {
            Set<String> locations = occurences.computeIfAbsent(key, k -> new LinkedHashSet<>());
            locations.add(location);
        }
    }
}
