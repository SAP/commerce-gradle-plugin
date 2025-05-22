package mpern.sap.commerce.build.util

import spock.lang.Specification

class VersionTest extends Specification {

    static Map<String, Integer> PREVIEW_TO_PLATFORM_PATCH = [
        "2211.FP0": 4,
        "2211.FP1": 8
    ]

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

    def "preview version can be parsed and is mapped to correct patch"() {
        when:
        def v = Version.parseVersion("2211.FP1", PREVIEW_TO_PLATFORM_PATCH)

        then:
        v.preview
        v.major == 22
        v.minor == 11
        v.patch == 8
        v.toString() == "2211.FP1 (PREVIEW) [2211.8]"
    }

    def "preview version without mapping is parsed correctly without patch"() {
        when:
        def v = Version.parseVersion("1905.FP1", PREVIEW_TO_PLATFORM_PATCH)

        then:
        v.preview
        v.major == 19
        v.minor == 05
        v.patch == Version.UNDEFINED_PART
        v.toString() == "1905.FP1 (PREVIEW)"
    }

    def "preview version can be parsed without patch when not needed"() {
        when:
        def v = Version.parseVersion("2211.FP1")

        then:
        v.preview
        v.major == 22
        v.minor == 11
        v.patch == Version.UNDEFINED_PART
        v.toString() == "2211.FP1 (PREVIEW)"
    }

    def "jdk21 preview version can be parsed without patch when not needed"() {
        when:
        def v = Version.parseVersion("2211-jdk21.FP10")

        then:
        v.preview
        v.major == 22
        v.minor == 11
        v.patch == Version.UNDEFINED_PART
        v.toString() == "2211-jdk21.FP10 (PREVIEW)"
    }

    def "jdk21 version with patch"() {
        when:
        def v = Version.parseVersion("2211-jdk21.1")

        then:
        !v.preview
        v.major == 22
        v.minor == 11
        v.patch == 1
        v.toString() == "2211-jdk21.1"
    }

    def "jdk21 version can have test build"() {
        when:
        def v = Version.parseVersion("2211-jdk21.TEST.20250505")

        then:
        !v.preview
        v.major == 22
        v.minor == 11
        v.patch == 20250505
        v.toString() == "2211-jdk21.TEST.20250505"
    }

    def "jdk21 version can have test build without date timestamp"() {
        when:
        def v = Version.parseVersion("2211-jdk21.TEST")

        then:
        !v.preview
        v.major == 22
        v.minor == 11
        v.patch == Version.UNDEFINED_PART
        v.toString() == "2211-jdk21.TEST"
    }

    def "1808 version should be used with Java 8"() {
        when:
        def v = Version.parseVersion("1808")

        then:
        v.jdk == 8
    }

    def "2105 version should be used with Java 11"() {
        when:
        def v = Version.parseVersion("2105")

        then:
        v.jdk == 11
    }

    def "2211 version should be used with Java 17"() {
        when:
        def v = Version.parseVersion("2211")

        then:
        v.jdk == 17
    }

    def "2211-jdk21 version should be used with Java 21"() {
        when:
        def v = Version.parseVersion("2211-jdk21")

        then:
        v.jdk == 21
    }
}
