package mpern.sap.commerce.ccv2.validation

import org.junit.Rule
import org.junit.rules.TemporaryFolder

import groovy.json.JsonSlurper
import spock.lang.Specification

import mpern.sap.commerce.ccv2.model.Manifest
import mpern.sap.commerce.ccv2.validation.impl.CloudHotfolderValidator
import mpern.sap.commerce.ccv2.validation.impl.ManifestExtensionsResolver

class CloudHotfolderValidatorSpec extends Specification {

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()


    def "hotfolder validator checks backgroundProcessing properties"() {
        given:
        def rawManifest = new JsonSlurper().parseText('''\
        {
          "commerceSuiteVersion": "2011",
          "extensions": [
            "azurecloudhotfolder"
          ],
          "useConfig": {
            "properties": [
              {
                "location": "background.properties",
                "aspect": "backgroundProcessing"
              }
            ]
          }
        }
        ''') as Map<String, Object>
        def manifest = Manifest.fromMap(rawManifest)
        def validator = new CloudHotfolderValidator(testProjectDir.root.toPath(), new ManifestExtensionsResolver(testProjectDir.root.toPath()))
        def props = testProjectDir.newFile("background.properties")

        when:
        def unconfiguredErrors = validator.validate(manifest)

        props.text = "cluster.node.groups=yHotfolderCandidate,integration"
        def configuredErrors = validator.validate(manifest)

        then:
        unconfiguredErrors.size() == 1
        unconfiguredErrors.any{ it.level == Level.WARNING && it.code == "W-003"}
        configuredErrors.isEmpty()
    }
}
