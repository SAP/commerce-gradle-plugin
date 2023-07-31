package mpern.sap.commerce.ccv2.validation

import groovy.json.JsonSlurper
import spock.lang.Specification

import mpern.sap.commerce.ccv2.model.Manifest
import mpern.sap.commerce.ccv2.validation.impl.AddonValidator

class AddonValidatorSpec extends Specification {

    def "addon validation checks available extensions"() {
        given:
        def rawManifest = new JsonSlurper().parseText('''\
        {
          "commerceSuiteVersion": "2005",
          "storefrontAddons": [
            {
              "addon": "spartacussampledataaddon,commerceorgsamplesaddon",
              "storefront": "yacceleratorstorefront,yb2bacceleratorstorefront",
              "template": "yacceleratorstorefront"
            },
            {
              "addons": ["smarteditaddon", "textfieldconfiguratortemplateaddon"],
              "storefront": "yacceleratorstorefront",
              "template": "yacceleratorstorefront"
            }
          ]
        }
        ''') as Map<String, Object>
        def manifest = Manifest.fromMap(rawManifest)
        def resolver = new TestExtensionResolver()
        resolver.addExtension("spartacussampledataaddon")
        resolver.addExtension("yacceleratorstorefront")
        resolver.addExtension("textfieldconfiguratortemplateaddon")
        def validator = new AddonValidator(resolver);

        when:
        def errors = validator.validate(manifest)

        then:
        errors.size() == 3
        errors.any{ it.level == Level.ERROR && it.message.contains("`yb2bacceleratorstorefront`")}
        errors.any{ it.level == Level.ERROR && it.message.contains("`commerceorgsamplesaddon`")}
        errors.any{ it.level == Level.ERROR && it.message.contains("`smarteditaddon`")}
    }
}
