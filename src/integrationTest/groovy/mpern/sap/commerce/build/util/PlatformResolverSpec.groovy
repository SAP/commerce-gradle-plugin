package mpern.sap.commerce.build.util

import java.nio.file.Files
import java.nio.file.Paths

import org.junit.AssumptionViolatedException

import spock.lang.Specification

class PlatformResolverSpec extends Specification {

    PlatformResolver resolver

    def setup() {
        def root = Paths.get("manualTest")
        def platformHome = root.resolve(Paths.get("hybris", "bin", "platform"))
        if(!Files.exists(platformHome)) {
            throw new AssumptionViolatedException("platform not available");
        }
        resolver = new PlatformResolver(platformHome)
    }

    def "load extensions correctly"() {
        when:
        def extensions = resolver.getConfiguredExtensions()
        println(extensions.size())
        println(extensions.sort{it.name}.collect{"${it.name} - ${it.directory}"}.join('\n'))

        then:
        extensions.size() > 0
    }
}
