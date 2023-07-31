package mpern.sap.commerce.ccv2.validation


import java.nio.file.Path

import groovy.json.JsonSlurper
import spock.lang.Specification
import spock.lang.TempDir

import mpern.sap.commerce.ccv2.model.Manifest
import mpern.sap.commerce.ccv2.validation.impl.CloudHotfolderValidator
import mpern.sap.commerce.ccv2.validation.impl.ManifestExtensionsResolver

class CloudHotfolderValidatorSpec extends Specification {

    @TempDir
    Path testProjectDir


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
        def validator = new CloudHotfolderValidator(testProjectDir, new ManifestExtensionsResolver(testProjectDir))
        def props = testProjectDir.resolve("background.properties")

        when:
        def unconfiguredErrors = validator.validate(manifest)

        props.text = "cluster.node.groups=yHotfolderCandidate,integration,backgroundProcessing,foo"
        def configuredErrors = validator.validate(manifest)

        then:
        unconfiguredErrors.size() == 1
        unconfiguredErrors.any{ it.level == Level.WARNING && it.code == "W-003"}
        configuredErrors.isEmpty()
    }
}
