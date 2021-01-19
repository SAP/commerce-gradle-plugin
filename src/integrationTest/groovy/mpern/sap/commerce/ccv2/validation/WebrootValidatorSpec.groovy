package mpern.sap.commerce.ccv2.validation

import java.nio.file.Paths

import groovy.json.JsonSlurper
import spock.lang.Specification

import mpern.sap.commerce.ccv2.model.Manifest
import mpern.sap.commerce.ccv2.validation.impl.WebrootValidator

class WebrootValidatorSpec extends Specification {
    def "extension.webroot is not allowed"() {
        given:
        def rawManifest = new JsonSlurper().parse(this.getClass().getResource('/validator/webroot-manifest.json')) as Map<String, Object>
        def manifest = Manifest.fromMap(rawManifest)
        def validator = new WebrootValidator(Paths.get("foo"))

        when:
        List<Error> webrootErrors = validator.validate(manifest)

        then:
        webrootErrors.size() == 1
        webrootErrors.get(0).location == "<multiple>"
        webrootErrors.get(0).message.contains("backoffice.webroot")
        webrootErrors.get(0).message.contains("hac.webroot")
    }
}
