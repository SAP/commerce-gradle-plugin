package mpern.sap.commerce.ccv2.validation.impl;

import static mpern.sap.commerce.ccv2.model.Aspect.ADMIN_ASPECT;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import mpern.sap.commerce.ccv2.model.Aspect;
import mpern.sap.commerce.ccv2.model.Manifest;
import mpern.sap.commerce.ccv2.model.Webapp;
import mpern.sap.commerce.ccv2.validation.Error;
import mpern.sap.commerce.ccv2.validation.ExtensionValidator;
import mpern.sap.commerce.ccv2.validation.ExtensionsResolver;

public class AspectWebappValidator extends ExtensionValidator {
    public AspectWebappValidator(ExtensionsResolver extensionsResolver) {
        super(extensionsResolver);
    }

    @Override
    protected List<Error> validateWithExtensions(Manifest manifest, ExtensionsResolver.Result effectiveExtensions) {
        List<Error> errors = new ArrayList<>();
        Set<String> extensionNames = effectiveExtensions.extensions.stream().map(e -> e.name)
                .collect(Collectors.toUnmodifiableSet());
        for (int i = 0; i < manifest.aspects.size(); i++) {
            Aspect aspect = manifest.aspects.get(i);
            if (ADMIN_ASPECT.equals(aspect.name)) {
                continue;
            }
            for (int j = 0; j < aspect.webapps.size(); j++) {
                Webapp w = aspect.webapps.get(j);
                // extension does not exist / not loaded
                if (!extensionNames.contains(w.name)) {
                    errors.add(
                            new Error.Builder().setLocation("aspects[?name == '%s'].webapps[%d]", aspect.name, i)
                                    .setMessage("Extension `%s` not available.\n%s", w.name,
                                            formatLocations(effectiveExtensions.locations))
                                    .setCode("E-001").createError());
                }
            }

        }
        return errors;
    }
}
