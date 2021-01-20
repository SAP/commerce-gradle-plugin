package mpern.sap.commerce.ccv2.validation

import groovy.json.JsonSlurper
import spock.lang.Specification

import mpern.sap.commerce.ccv2.model.Manifest
import mpern.sap.commerce.ccv2.validation.impl.CloudExtensionPackValidator

class CloudExtensionPackValidatorSpec extends Specification {


    def "CEP validation checks for patch version"() {
        def rawManifest = new JsonSlurper().parseText('''\
        {
          "commerceSuiteVersion": "1905.5",
          "useCloudExtensionPack": true
        }
        ''') as Map<String, Object>
        def manifest = Manifest.fromMap(rawManifest)
        def validator = new CloudExtensionPackValidator()

        when:
        def errors = validator.validate(manifest)

        then:
        errors.size() == 1
        errors.any{ it.location == 'useCloudExtensionPack' && it.message.contains("patch") && it.message.contains(".5")}
    }
}
