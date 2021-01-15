package mpern.sap.commerce.ccv2.validation

import groovy.json.JsonSlurper
import spock.lang.Specification

import mpern.sap.commerce.ccv2.model.Manifest
import mpern.sap.commerce.ccv2.validation.impl.CloudExtensionPackValidator

class CloudExtensionPackValidatorSpec extends Specification {
    Manifest manifest
    CloudExtensionPackValidator validator

    def setup() {
        def rawManifest = new JsonSlurper().parse(this.getClass().getResource('/validator/cep-manifest.json')) as Map<String, Object>
        manifest = Manifest.fromMap(rawManifest)
        validator = new CloudExtensionPackValidator()
    }

    def "CEP validation"() {
        when:
        List<Error> errors = validator.validate(manifest)

        then:
        errors.size() == 1
    }
}
