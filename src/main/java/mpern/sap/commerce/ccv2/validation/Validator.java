package mpern.sap.commerce.ccv2.validation;

import java.util.List;

import mpern.sap.commerce.ccv2.model.Manifest;

public interface Validator {

    List<Error> validate(Manifest manifest) throws Exception;

}
