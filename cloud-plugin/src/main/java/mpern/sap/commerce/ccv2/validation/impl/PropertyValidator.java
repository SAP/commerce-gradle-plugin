package mpern.sap.commerce.ccv2.validation.impl;

import java.util.List;

import mpern.sap.commerce.ccv2.model.Manifest;
import mpern.sap.commerce.ccv2.validation.Error;
import mpern.sap.commerce.ccv2.validation.Validator;

public class PropertyValidator implements Validator {
    @Override
    public List<Error> validate(Manifest manifest) throws Exception {
        return new SharedPropertyValidator("").validateProperties(manifest.properties);
    }
}
