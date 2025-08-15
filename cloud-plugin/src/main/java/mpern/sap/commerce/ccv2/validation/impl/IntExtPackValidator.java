package mpern.sap.commerce.ccv2.validation.impl;

import java.util.*;

import mpern.sap.commerce.build.util.Version;
import mpern.sap.commerce.ccv2.model.ExtensionPack;
import mpern.sap.commerce.ccv2.model.Manifest;
import mpern.sap.commerce.ccv2.validation.Error;
import mpern.sap.commerce.ccv2.validation.Validator;

public class IntExtPackValidator implements Validator {

    private static final String PACK = "hybris-commerce-integrations";

    private static final Map<Version, Version> PLATFORM_TO_PACK;

    static {
        PLATFORM_TO_PACK = new HashMap<>();
        PLATFORM_TO_PACK.put(Version.parseVersion("2005"), Version.parseVersion("2005"));
        PLATFORM_TO_PACK.put(Version.parseVersion("2011"), Version.parseVersion("2102"));
        PLATFORM_TO_PACK.put(Version.parseVersion("2105"), Version.parseVersion("2108"));
        PLATFORM_TO_PACK.put(Version.parseVersion("2205"), Version.parseVersion("2205"));
        PLATFORM_TO_PACK.put(Version.parseVersion("2211"), Version.parseVersion("2211"));
    }

    @Override
    public List<Error> validate(Manifest manifest) throws Exception {
        if (manifest.extensionPacks.isEmpty()) {
            return Collections.emptyList();
        }
        List<Error> allErrors = new ArrayList<>();
        List<ExtensionPack> extensionPacks = manifest.extensionPacks;
        for (int i = 0, extensionPacksSize = extensionPacks.size(); i < extensionPacksSize; i++) {
            ExtensionPack extensionPack = extensionPacks.get(i);
            String name;
            String versionString;
            if (extensionPack.name.isEmpty()) {
                final String[] split = extensionPack.artifact.split(":");
                name = split[1].trim();
                versionString = split[2].trim();
            } else {
                name = extensionPack.name;
                versionString = extensionPack.version;
            }
            if (PACK.equals(name)) {
                Version platform = Version.parseVersion(manifest.getEffectiveVersion()).withoutPatch();
                Version pack = Version.parseVersion(versionString).withoutPatch();
                Version expected = PLATFORM_TO_PACK.get(platform);
                List<Error> errors = new ArrayList<>();
                if (!pack.equals(PLATFORM_TO_PACK.get(platform))) {
                    String message;
                    if (expected == null) {
                        message = String.format("No Integration Extension Pack available for SAP Commerce %s",
                                manifest.getEffectiveVersion());
                    } else {
                        message = String.format("Integration Extension Pack %s is not compatible with SAP Commerce %s",
                                extensionPack.version, manifest.getEffectiveVersion());
                    }
                    errors.add(new Error.Builder().setLocation("extensionPacks[%d]", i).setCode("E-019")
                            .setMessage(message).createError());
                }
                final Version version = Version.parseVersion(versionString);
                if (version.getPatch() == Version.UNDEFINED_PART) {
                    errors.add(new Error.Builder().setLocation("extensionPacks[%d]", i).setCode("E-019").setMessage(
                            "Integration Extension Pack version %s is not fully qualified (does not include patch)",
                            version).createError());
                }
                allErrors.addAll(errors);
            }
        }
        return allErrors;
    }
}
