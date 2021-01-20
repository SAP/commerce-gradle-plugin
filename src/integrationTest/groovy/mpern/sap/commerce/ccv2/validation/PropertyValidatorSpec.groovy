package mpern.sap.commerce.ccv2.validation

import groovy.json.JsonSlurper
import spock.lang.Specification

import mpern.sap.commerce.ccv2.model.Manifest
import mpern.sap.commerce.ccv2.validation.impl.PropertyValidator

class PropertyValidatorSpec extends Specification {

    def "manifest properties are validated"() {
        given:
        def rawManifest = new JsonSlurper().parseText('''\
        {
          "commerceSuiteVersion": "2011.1",
          "properties": [
            {
              "key": "clustermode",
              "value": "false"
            },
            {
              "key": "persona.property",
              "value": "wrong persona",
              "persona": "invalid"
            },
            {
              "key": "valid.property",
              "value": "value",
              "persona": "production"
            }
          ]
        }
        ''') as Map<String, Object>
        def manifest = Manifest.fromMap(rawManifest)
        def validator = new PropertyValidator()

        when:
        def errors = validator.validate(manifest);

        then:
        errors.size() == 2
        errors.any{ it.level == Level.WARNING && it.location == 'properties[0]' && it.message.contains("`clustermode`")}
        errors.any{ it.level == Level.ERROR && it.location == 'properties[1]' && it.message.contains("`invalid`")}
    }
}
