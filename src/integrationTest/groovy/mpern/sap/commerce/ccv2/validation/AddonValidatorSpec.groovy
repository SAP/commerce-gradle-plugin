package mpern.sap.commerce.ccv2.validation

import groovy.json.JsonSlurper
import spock.lang.Specification

import mpern.sap.commerce.ccv2.model.Manifest
import mpern.sap.commerce.ccv2.validation.impl.AddonValidator

class AddonValidatorSpec extends Specification {

    Manifest manifest
    AddonValidator validator

    def setup() {
        def rawManifest = new JsonSlurper().parse(this.getClass().getResource('/validator/addon-manifest.json')) as Map<String, Object>
        manifest = Manifest.fromMap(rawManifest)
        TestExtensionResolver resolver = new TestExtensionResolver()
        resolver.addExtension("spartacussampledataaddon")
        resolver.addExtension("yacceleratorstorefront")
        resolver.addExtension("textfieldconfiguratortemplateaddon")
        validator = new AddonValidator(resolver);
    }

    def "addon validation checks available extensions"() {
        when:
        List<Error> errors = validator.validate(manifest)
        errors.forEach {
            System.out.println(it)
        }

        then:
        errors.size() == 3
        errors.any{ it.level == Level.ERROR && it.message.contains("`yb2bacceleratorstorefront`")}
        errors.any{ it.level == Level.ERROR && it.message.contains("`commerceorgsamplesaddon`")}
        errors.any{ it.level == Level.ERROR && it.message.contains("`smarteditaddon`")}
    }
}
