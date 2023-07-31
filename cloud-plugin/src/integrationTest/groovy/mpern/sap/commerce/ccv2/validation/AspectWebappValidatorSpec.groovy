package mpern.sap.commerce.ccv2.validation

import groovy.json.JsonSlurper
import spock.lang.Specification

import mpern.sap.commerce.ccv2.model.Manifest
import mpern.sap.commerce.ccv2.validation.Error
import mpern.sap.commerce.ccv2.validation.Level
import mpern.sap.commerce.ccv2.validation.impl.AspectWebappValidator

class AspectWebappValidatorSpec extends Specification {

    def "webapps are checked against effective extensions"() {
        given:
        def rawManifest = new JsonSlurper().parseText('''\
        {
          "commerceSuiteVersion": "2011.1",
          "aspects": [
            {
              "name": "accstorefront",
              "webapps": [
                {
                  "name": "albinostorefront",
                  "contextPath": ""
                },
                {
                  "name": "otherstorefront",
                  "contextPath": ""
                },
                {
                  "name": "mediaweb",
                  "contextPath": "media"
                }
              ]
            }
          ]
        }
        ''') as Map<String, Object>
        def manifest = Manifest.fromMap(rawManifest)
        def resolver = new TestExtensionResolver();
        resolver.addExtension("mediaweb")
        resolver.addExtension("albinostorefront")
        def validator = new AspectWebappValidator(resolver);

        when:
        def errors = validator.validate(manifest)

        then:
        errors.size() == 1
        errors.any{ it.level == Level.ERROR && it.message.contains('`otherstorefront`') }
    }
}
