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
        v.patch == Version.UNDEFINED_PART
        v.toString() == "1905"
    }

    def "preview version can be parsed"() {
        when:
        def v = Version.parseVersion("2211.FP1")

        then:
        v.preview
        v.major == 22
        v.minor == 11
        v.patch == Integer.MIN_VALUE
        v.toString() == "2211.FP1 (PREVIEW)"
    }
}
