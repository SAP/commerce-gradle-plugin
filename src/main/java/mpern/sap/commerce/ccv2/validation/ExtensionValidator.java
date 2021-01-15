package mpern.sap.commerce.ccv2.validation;

import java.util.Collections;
import java.util.List;

import mpern.sap.commerce.ccv2.model.Manifest;

public abstract class ExtensionValidator implements Validator {

    protected final ExtensionsResolver resolver;

    public ExtensionValidator(ExtensionsResolver extensionsResolver) {
        this.resolver = extensionsResolver;
    }

    @Override
    public List<Error> validate(Manifest manifest) throws Exception {
        ExtensionsResolver.Result result = resolver.determineEffectiveExtensions(manifest);
        if (result == ExtensionsResolver.Result.NO_RESULT) {
            return Collections.emptyList();
        } else {
            return validateWithExtensions(manifest, result);
        }
    }

    protected abstract List<Error> validateWithExtensions(Manifest manifest,
            ExtensionsResolver.Result effectiveExtensions);

    protected String formatLocations(List<String> locations) {
        return "Extensions loaded from:\n- " + String.join("\n- ", locations);
    }
}
