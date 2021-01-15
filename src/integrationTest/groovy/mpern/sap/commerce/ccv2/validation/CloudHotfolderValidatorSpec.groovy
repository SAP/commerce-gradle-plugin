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
        def rawManifest = new JsonSlurper().parse(this.getClass().getResource('/validator/cloudhotfolder-manifest.json')) as Map<String, Object>
        def manifest = Manifest.fromMap(rawManifest)
        CloudHotfolderValidator validator = new CloudHotfolderValidator(testProjectDir.root.toPath(), new ManifestExtensionsResolver(testProjectDir.root.toPath()))
        def props = testProjectDir.newFile("background.properties")

        when:
        List<Error> unconfigured = validator.validate(manifest)

        props.text = "cluster.node.groups=yHotfolderCandidate,integration"
        List<Error> configured = validator.validate(manifest)

        then:
        unconfigured.size() == 1
        configured.isEmpty()
    }
}
