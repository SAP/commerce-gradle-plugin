package mpern.sap.commerce.ccv2.validation

import groovy.json.JsonSlurper
import spock.lang.Specification

import mpern.sap.commerce.ccv2.model.Manifest
import mpern.sap.commerce.ccv2.validation.impl.MediaConversionValidator

class MediaConversionValidatorSpec extends Specification {
    def "media conversion requires both service and extension"() {
        given:
        def rawManifest = new JsonSlurper().parseText('''\
        {
          "commerceSuiteVersion": "2011",
          "enableImageProcessingService": true
        }
        ''') as Map<String, Object>
        def serviceOnlyManifest = Manifest.fromMap(rawManifest)

        rawManifest = new JsonSlurper().parseText('''\
        {
          "commerceSuiteVersion": "2011",
          "extensions": [
            "cloudmediaconversion"
          ]
        }
        ''') as Map<String, Object>
        def extensionOnlyManifest = Manifest.fromMap(rawManifest)
        def testResolver = new TestExtensionResolver()
        def validator = new MediaConversionValidator(testResolver)

        when:
        def withoutExtensions = validator.validate(serviceOnlyManifest)

        testResolver.addExtension("cloudmediaconversion")
        def withExtension = validator.validate(serviceOnlyManifest)

        def withoutService = validator.validate(extensionOnlyManifest)

        then:
        withoutExtensions.size() == 1
        withExtension.isEmpty()
        withoutService.size() == 1
    }
}
