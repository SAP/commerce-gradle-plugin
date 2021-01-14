package mpern.sap.commerce.ccv2.validation.impl;

import java.util.ArrayList;
import java.util.List;

import mpern.sap.commerce.build.util.Version;
import mpern.sap.commerce.ccv2.model.Manifest;
import mpern.sap.commerce.ccv2.validation.Error;
import mpern.sap.commerce.ccv2.validation.Validator;

public class CloudExtensionPackValidator implements Validator {
    @Override
    public List<Error> validate(Manifest manifest) throws Exception {
        List<Error> errors = new ArrayList<>();
        if (manifest.useCloudExtensionPack) {
            Version v = Version.parseVersion(manifest.commerceSuiteVersion);
            if (v.compareTo(Version.parseVersion("1811.0")) < 0 || v.compareTo(Version.parseVersion("1911")) > 0) {
                errors.add(new Error.Builder().setLocation("useCloudExtensionPack")
                        .setMessage("Version `%s` does not support Cloud Extension Pack", v).setCode("E-014")
                        .createError());
            }
            if (v.getPatch() != Integer.MAX_VALUE) {
                errors.add(new Error.Builder().setLocation("useCloudExtensionPack").setMessage(
                        "Configuring a specific patch release (`.%d`) is not allowed when using Cloud Extension Pack",
                        v.getPatch()).setCode("E-015").createError());
            }
        }
        return errors;
    }
}
