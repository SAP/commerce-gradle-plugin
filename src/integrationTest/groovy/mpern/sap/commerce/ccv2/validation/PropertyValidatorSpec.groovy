package mpern.sap.commerce.ccv2.validation

import groovy.json.JsonSlurper
import spock.lang.Specification

import mpern.sap.commerce.ccv2.model.Manifest
import mpern.sap.commerce.ccv2.validation.impl.PropertyValidator

class PropertyValidatorSpec extends Specification {
    Manifest manifest
    PropertyValidator validator

    def setup() {
        def rawManifest = new JsonSlurper().parse(this.getClass().getResource('/validator/property-manifest.json')) as Map<String, Object>
        manifest = Manifest.fromMap(rawManifest)
        validator = new PropertyValidator()
    }

    def "manifest properties are validated"() {
        when:
        List<Error> errors = validator.validate(manifest);
        errors.forEach{
            System.out.println(it)
        }

        then:
        errors.size() == 2
        errors.any{ it.level == Level.WARNING && it.message.contains("`clustermode`")}
        errors.any{ it.level == Level.ERROR && it.message.contains("`invalid`")}
    }
}
