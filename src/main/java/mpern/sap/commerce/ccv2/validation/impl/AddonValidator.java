package mpern.sap.commerce.ccv2.validation.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import mpern.sap.commerce.ccv2.model.Addon;
import mpern.sap.commerce.ccv2.model.Manifest;
import mpern.sap.commerce.ccv2.validation.Error;
import mpern.sap.commerce.ccv2.validation.ExtensionValidator;
import mpern.sap.commerce.ccv2.validation.ExtensionsResolver;

public class AddonValidator extends ExtensionValidator {

    public AddonValidator(ExtensionsResolver extensionsResolver) {
        super(extensionsResolver);
    }

    @Override
    protected List<Error> validateWithExtensions(Manifest manifest, ExtensionsResolver.Result effectiveExtensions) {
        Set<String> extensionNames = effectiveExtensions.extensions.stream().map(e -> e.name)
                .collect(Collectors.toSet());
        List<Error> errors = new ArrayList<>();
        for (int i = 0; i < manifest.storefrontAddons.size(); i++) {
            Addon addon = manifest.storefrontAddons.get(i);
            List<String> storefronts = new ArrayList<>(addon.storefronts);
            if (!addon.storefront.isEmpty()) {
                storefronts.addAll(Arrays.asList(addon.storefront.split(",")));
            }
            for (String s : storefronts) {
                if (!extensionNames.contains(s)) {
                    errors.add(
                            new Error.Builder().setLocation("storefrontAddons[%d]", i)
                                    .setMessage("Storefront extension `%s` not available.\n%s", s,
                                            formatLocations(effectiveExtensions.locations))
                                    .setCode("E-001").createError());
                }
            }
            List<String> addons = new ArrayList<>(addon.addons);
            if (!addon.addon.isEmpty()) {
                addons.addAll(Arrays.asList(addon.addon.split(",")));
            }
            for (String a : addons) {
                if (!extensionNames.contains(a)) {
                    errors.add(
                            new Error.Builder().setLocation("storefrontAddons[%d]", i)
                                    .setMessage("Addon `%s` not available.\n%s", a,
                                            formatLocations(effectiveExtensions.locations))
                                    .setCode("E-001").createError());
                }
            }
        }
        return errors;
    }
}
