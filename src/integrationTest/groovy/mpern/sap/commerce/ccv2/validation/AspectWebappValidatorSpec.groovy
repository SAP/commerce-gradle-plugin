package mpern.sap.commerce.ccv2.validation

import groovy.json.JsonSlurper
import spock.lang.Specification

import mpern.sap.commerce.ccv2.model.Manifest
import mpern.sap.commerce.ccv2.validation.Error
import mpern.sap.commerce.ccv2.validation.Level
import mpern.sap.commerce.ccv2.validation.impl.AspectWebappValidator

class AspectWebappValidatorSpec extends Specification {

    AspectWebappValidator validator
    Manifest manifest

    def setup() {
        def rawManifest = new JsonSlurper().parse(this.getClass().getResource('/validator/aspect-manifest.json')) as Map<String, Object>
        manifest = Manifest.fromMap(rawManifest)
        TestExtensionResolver resolver = new TestExtensionResolver();
        resolver.addExtension("hac")
        resolver.addExtension("mediaweb")
        resolver.addExtension("backoffice")
        resolver.addExtension("acceleratorservices")
        resolver.addExtension("albinostorefront")
        validator = new AspectWebappValidator(resolver);
    }

    def "webapps are checked against effective extensions"() {
        when:
        List<Error> errors = validator.validate(manifest)
        errors.forEach{
            System.out.println(it)
        }

        then:
        errors.size() == 1
        errors.any{ it.level == Level.ERROR && it.message.contains('`otherstorefront`') }
    }
}
