package mpern.sap.commerce.build.util

import spock.lang.Specification

class VersionTest extends Specification {

    def "version parsed correctly"() {
        when:
        def v = Version.parseVersion('1905.2')

        then:
        v.major == 19
        v.minor == 05
        v.release == 0
        v.patch == 2
        v.toString() == "1905.2"

    }

    def "version without patch parsed correctly"() {
        when:
        def v = Version.parseVersion('1905')

        then:
        v.major == 19
        v.minor == 05
        v.release == 0
        v.patch == Integer.MAX_VALUE
        v.toString() == "1905"
    }
}
