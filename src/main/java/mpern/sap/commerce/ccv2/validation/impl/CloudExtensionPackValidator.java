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
                        .setMessage("Version `%s` does not support cloud extension pack", v)
                        .setLink(
                                "https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/v1905/en-US/3d562b85b37a460a92d32ec991459133.html")
                        .createError());
            }
            if (v.getPatch() != Integer.MAX_VALUE) {
                errors.add(new Error.Builder().setLocation("useCloudExtensionPack")
                        .setMessage("No patch release (`.%d`) allowed when using cloud extension pack", v.getPatch())
                        .setLink(
                                "https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/v1905/en-US/3d562b85b37a460a92d32ec991459133.html")
                        .createError());
            }
        }
        return errors;
    }
}
