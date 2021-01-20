package mpern.sap.commerce.ccv2.validation

import groovy.json.JsonSlurper
import spock.lang.Specification

import mpern.sap.commerce.ccv2.model.Manifest
import mpern.sap.commerce.ccv2.validation.impl.AspectValidator

class AspectValidatorSpec extends Specification {

    def "validating aspects detects errors"() {
        given:
        def rawManifest = new JsonSlurper().parseText('''\
        {
          "commerceSuiteVersion": "2011.1",
          "aspects": [
            { "name":  "invalid" },
            {
              "name": "backoffice",
              "properties": [
                {
                  "key": "spring.session.enabled",
                  "value": "false"
                }
              ],
              "webapps": [
                {
                  "name": "hac",
                  "contextPath": "/hac"
                },
                {
                  "name": "hac",
                  "contextPath": "/other"
                }
              ]
            },
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
            },
            {
              "name": "admin",
              "webapps": [
                {
                  "name": "storefront",
                  "contextPath": ""
                }
              ]
            }
          ]
        }
        ''') as Map<String, Object>
        def manifest = Manifest.fromMap(rawManifest)
        def validator = new AspectValidator();

        when:
        def errors = validator.validate(manifest)

        then:
        errors.size() == 6
        errors.any {it.level == Level.ERROR && it.location == "aspects[?name == 'invalid']" && it.message.contains("not supported")}
        errors.any {it.level == Level.ERROR && it.location == "aspects[?name == 'admin']" && it.message.contains("Webapps")}
        errors.any {it.level == Level.ERROR && it.location == "aspects[?name == 'accstorefront'].webapps[1]" && it.message.contains("Context path ``")}
        errors.any {it.level == Level.ERROR && it.location == "aspects[?name == 'backoffice'].webapps[1]" && it.message.contains("Extension `hac`")}
        errors.any {it.level == Level.WARNING && it.location == "aspects[?name == 'backoffice'].properties[0]" && it.message.contains('`spring.session.enabled`')}
        errors.any {it.level == Level.ERROR && it.location == "aspects[?name == 'accstorefront'].webapps[2]" && it.message.contains("`/`")}
    }
}
