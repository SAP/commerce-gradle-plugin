package mpern.sap.commerce.ccv2.validation

import org.junit.Rule
import org.junit.rules.TemporaryFolder

import groovy.json.JsonSlurper
import spock.lang.Specification

import mpern.sap.commerce.ccv2.model.Manifest
import mpern.sap.commerce.ccv2.validation.impl.WebrootValidator

class WebrootValidatorSpec extends Specification {

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    def "extension.webroot is not allowed"() {
        given:
        def rawManifest = new JsonSlurper().parseText('''\
        {
          "commerceSuiteVersion": "1905.5",
          "properties": [
            {
              "key": "hac.webroot",
              "value": "/hac"
            }
          ],
          "useConfig": {
            "properties": [
              {
                "location": "webroot.properties"
              }
            ]
          },
          "aspects": [
            {
              "name": "backoffice",
              "properties": [
                {
                  "key": "backoffice.webroot",
                  "value": "/foo",
                  "persona": "production"
                }
              ]
            }
          ]
        }
        ''') as Map<String, Object>
        def manifest = Manifest.fromMap(rawManifest)
        def validator = new WebrootValidator(testProjectDir.root.toPath())
        testProjectDir.newFile("webroot.properties").text = '''\
        demostorefront.webroot=/root
        '''.stripIndent()

        when:
        def webrootErrors = validator.validate(manifest)

        then:
        webrootErrors.size() == 3
        webrootErrors.any{it.code == "E-017" && it.location == 'properties[0]' && it.message.contains("hac.webroot")}
        webrootErrors.any{it.code == "E-017" && it.location == "aspects[?name == 'backoffice'].properties[0]" && it.message.contains("backoffice.webroot")}
        webrootErrors.any{it.code == "E-017" && it.location == 'useConfig.properties[0].location (webroot.properties)' && it.message.contains("demostorefront.webroot")}
    }
}
