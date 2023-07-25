package mpern.sap.commerce.build.util

import java.nio.file.Files
import java.nio.file.Path

import spock.lang.IgnoreIf
import spock.lang.Specification

class PlatformResolverSpec extends Specification {

    def platformHome = Path.of("manualTest/hybris/bin/platform");
    PlatformResolver resolver

    def setup() {
        resolver = new PlatformResolver(platformHome)
    }

    @IgnoreIf({ !Files.exists(instance.platformHome) })
    def "load extensions correctly"() {
        when:
        def extensions = resolver.getConfiguredExtensions().collect()
        println(extensions.size())
        println(extensions.sort{it.name}.collect{"${it.name} - ${it.directory}"}.join('\n'))

        then:
        extensions.size() > 0
    }
}
