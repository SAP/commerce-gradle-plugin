package mpern.sap.commerce.ccv2.validation

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.junit.AssumptionViolatedException

import groovy.json.JsonSlurper
import spock.lang.Specification

import mpern.sap.commerce.ccv2.model.Manifest
import mpern.sap.commerce.ccv2.validation.impl.ManifestExtensionsResolver

class ExtensionResolverSpec extends Specification {

    Path projectRoot = Paths.get("manualTest")

    def "test extension resolver"() {
        given:
        if(!Files.exists(projectRoot.resolve(Paths.get("hybris/bin/platform")))) {
            throw new AssumptionViolatedException("platform not available for tests");
        }
        def resolver = new ManifestExtensionsResolver(projectRoot)
        def rawManifest = new JsonSlurper().parse(projectRoot.resolve("manifest.json").toFile(), "UTF-8") as Map<String, Object>
        def manifest = Manifest.fromMap(rawManifest)

        when:
        def result = resolver.determineEffectiveExtensions(manifest)

        then:
        !result.extensions.isEmpty()
        result.locations.size() == 2
        result.locations.any{it.contains("useConfig.extensions.location")}
    }
}
