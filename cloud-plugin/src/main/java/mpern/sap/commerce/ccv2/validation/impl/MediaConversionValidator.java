package mpern.sap.commerce.ccv2.validation.impl;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import groovy.lang.Tuple2;

import mpern.sap.commerce.ccv2.model.Manifest;
import mpern.sap.commerce.ccv2.validation.Error;
import mpern.sap.commerce.ccv2.validation.ExtensionsResolver;
import mpern.sap.commerce.ccv2.validation.Validator;

public class MediaConversionValidator implements Validator {
    private static final String CONVERSION_EXTENSION = "cloudmediaconversion";

    private final ExtensionsResolver resolver;

    public MediaConversionValidator(ExtensionsResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public List<Error> validate(Manifest manifest) throws Exception {
        Tuple2<Set<String>, List<String>> result = resolver.listAllConfiguredExtensions(manifest);
        boolean extensionFound = result.getV1().contains(CONVERSION_EXTENSION);
        boolean conversionEnabled = manifest.enableImageProcessingService;

        if (extensionFound && !conversionEnabled) {
            return Collections.singletonList(new Error.Builder().setLocation("enableImageProcessingService")
                    .setMessage("Extension `%s` configured, but image processing service is not enabled.",
                            CONVERSION_EXTENSION)
                    .setCode("E-016").createError());
        } else if (!extensionFound && conversionEnabled) {
            return Collections.singletonList(new Error.Builder().setLocation("enableImageProcessingService")
                    .setMessage("Image processing service is enabled, but extension `%s` not configured",
                            CONVERSION_EXTENSION)
                    .setCode("E-016").createError());
        } else {
            return Collections.emptyList();
        }
    }
}
