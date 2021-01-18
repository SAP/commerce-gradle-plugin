package mpern.sap.commerce.ccv2.validation

import groovy.json.JsonSlurper
import spock.lang.Specification

import mpern.sap.commerce.ccv2.model.Manifest
import mpern.sap.commerce.ccv2.validation.impl.MediaConversionValidator

class MediaConversionValidatorSpec extends Specification {
    def "media conversion requires both service and extension"() {
        given:
        def rawManifest = new JsonSlurper().parse(this.getClass().getResource('/validator/mediaconversion-manifest.json')) as Map<String, Object>
        def manifest = Manifest.fromMap(rawManifest)
        rawManifest = new JsonSlurper().parse(this.getClass().getResource('/validator/mediaconversion-no-service-manifest.json')) as Map<String, Object>
        def manifest2 = Manifest.fromMap(rawManifest)
        def testResolver = new TestExtensionResolver()
        def validator = new MediaConversionValidator(testResolver)

        when:
        def withoutExtensions = validator.validate(manifest)
        testResolver.addExtension("cloudmediaconversion")
        def withExtension = validator.validate(manifest)
        def withoutService = validator.validate(manifest2)

        then:
        withoutExtensions.size() == 1
        withExtension.isEmpty()
        withoutService.size() == 1
    }
}
