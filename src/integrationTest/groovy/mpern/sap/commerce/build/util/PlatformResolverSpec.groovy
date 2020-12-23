package mpern.sap.commerce.build.util

import java.nio.file.Paths

import spock.lang.Ignore
import spock.lang.Specification

class PlatformResolverSpec extends Specification {

    PlatformResolver resolver

    def setup() {
        def root = Paths.get("/path/to/commerce-gradle-plugin/manualTest")
        resolver = new PlatformResolver(root.resolve(Paths.get("hybris", "bin", "platform")))
    }

    @Ignore
    def "load extensions correctly"() {
        when:
        def extensions = resolver.getConfiguredExtensions()
        println(extensions.size())
        println(extensions.sort{it.name}.collect{"${it.name} - ${it.directory}"}.join('\n'))

        then:
        extensions.size() > 0
    }
}
