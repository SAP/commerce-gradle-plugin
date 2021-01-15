package mpern.sap.commerce.ccv2.validation.impl;

import static mpern.sap.commerce.ccv2.model.Aspect.ADMIN_ASPECT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mpern.sap.commerce.ccv2.model.Aspect;
import mpern.sap.commerce.ccv2.model.Manifest;
import mpern.sap.commerce.ccv2.model.Webapp;
import mpern.sap.commerce.ccv2.validation.Error;
import mpern.sap.commerce.ccv2.validation.Validator;

public class AspectValidator implements Validator {

    @Override
    public List<Error> validate(Manifest manifest) throws Exception {
        List<Error> errors = new ArrayList<>();
        Map<String, Integer> seenAspects = new HashMap<>();
        for (int aspectIndex = 0; aspectIndex < manifest.aspects.size(); aspectIndex++) {
            Aspect aspect = manifest.aspects.get(aspectIndex);
            if (!Aspect.ALLOWED_ASPECTS.contains(aspect.name)) {
                errors.add(new Error.Builder().setLocation("aspects[?name == '%s']", aspect.name)
                        .setMessage("Aspect `%s` not supported", aspect.name).setCode("E-002").createError());
            } else {
                Integer previous = seenAspects.put(aspect.name, aspectIndex);
                if (previous != null) {
                    errors.add(new Error.Builder().setLocation("aspects[%d]", aspectIndex)
                            .setMessage("Aspect `%s` configured more than once. Previous location: `aspects[%d]",
                                    aspect.name, previous)
                            .setCode("E-003").createError());
                }
                errors.addAll(new SharedPropertyValidator(String.format("aspects[?name == '%s'].", aspect.name))
                        .validateProperties(aspect.properties));
                if (ADMIN_ASPECT.equals(aspect.name)) {
                    if (!aspect.webapps.isEmpty()) {
                        errors.add(new Error.Builder().setLocation("aspects[?name == '%s']", aspect.name)
                                .setMessage("Webapps not allowed for aspect `admin`").setCode("E-007").createError());
                    }
                } else {
                    Map<String, Integer> loadedExtensions = new HashMap<>();
                    Map<String, Integer> webroots = new HashMap<>();
                    for (int j = 0; j < aspect.webapps.size(); j++) {
                        Webapp w = aspect.webapps.get(j);
                        previous = loadedExtensions.put(w.name, j);
                        if (previous != null) {
                            errors.add(new Error.Builder()
                                    .setLocation("aspects[?name == '%s'].webapps[%d]", aspect.name, j)
                                    .setMessage(
                                            "Extension `%s` configured more than once. Previous location: `aspects[?name == '%s'].webapps[%d]`",
                                            w.name, aspect.name, previous)
                                    .setCode("E-004").createError());
                        }
                        previous = webroots.put(w.contextPath, j);
                        if (previous != null) {
                            errors.add(new Error.Builder()
                                    .setLocation("aspects[?name == '%s'].webapps[%d]", aspect.name, j)
                                    .setMessage(
                                            "Context path `%s` configured more than once! Previous location: `aspects[?name == '%s'].webapps[%d]`",
                                            w.contextPath, aspect.name, previous)
                                    .setCode("E-005").createError());
                        }
                        if (!w.contextPath.isEmpty() && !w.contextPath.startsWith("/")) {
                            errors.add(new Error.Builder()
                                    .setLocation("aspects[?name == '%s'].webapps[%d]", aspect.name, j)
                                    .setMessage("contextPath `%s` must start with `/`", w.contextPath).setCode("E-006")
                                    .createError());
                        }
                    }
                }
            }
        }
        return errors;
    }
}
