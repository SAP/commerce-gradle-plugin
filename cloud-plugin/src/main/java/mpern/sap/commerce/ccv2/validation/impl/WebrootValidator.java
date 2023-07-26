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
            String location = String.format("properties[%d]", i);
            checkProperty(p.key, location, occurences);
        }
        for (int i = 0; i < manifest.useConfig.properties.size(); i++) {
            Properties properties = manifest.useConfig.properties.get(i);
            Tuple2<Path, List<Error>> result = ValidationUtils.validateAndNormalizePath(this.projectRoot, "",
                    properties.location);
            if (result.getV1() != null) {
                try (InputStream stream = Files.newInputStream(result.getV1())) {
                    java.util.Properties props = new java.util.Properties();
                    props.load(stream);
                    String location = String.format("useConfig.properties[%d].location (%s)", i, properties.location);
                    for (Map.Entry<Object, Object> entry : props.entrySet()) {
                        checkProperty((String) entry.getKey(), location, occurences);
                    }
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        for (Aspect aspect : manifest.aspects) {
            for (int i = 0; i < aspect.properties.size(); i++) {
                Property p = aspect.properties.get(i);
                String location = String.format("aspects[?name == '%s'].properties[%d]", aspect.name, i);
                checkProperty(p.key, location, occurences);
            }
        }
        if (occurences.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<Error> errors = new ArrayList<>();
            for (Map.Entry<String, Set<String>> errorEntry : occurences.entrySet()) {
                errors.add(new Error.Builder().setLocation(errorEntry.getKey()).setCode("E-017")
                        .setMessage("Do not configure webroots in properties.\nFaulty properties:\n- "
                                + String.join("\n -", errorEntry.getValue()))
                        .createError());
            }
            return errors;
        }
    }

    private void checkProperty(String key, String location, Map<String, Set<String>> occurences) {
        key = key.trim();
        if (key.endsWith(".webroot")) {
            Set<String> properties = occurences.computeIfAbsent(location, k -> new LinkedHashSet<>());
            properties.add(key);
        }
    }
}
