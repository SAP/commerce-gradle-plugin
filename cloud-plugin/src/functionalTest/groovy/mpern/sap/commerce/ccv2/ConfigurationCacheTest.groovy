package mpern.sap.commerce.ccv2

import java.nio.file.Files
import java.nio.file.Path

import org.gradle.testkit.runner.GradleRunner

import spock.lang.Specification
import spock.lang.TempDir

import mpern.sap.commerce.test.TestConstants

class ConfigurationCacheTest extends Specification {

    @TempDir
    Path testProjectPath

    GradleRunner runner

    def setup() {
        def buildFile = testProjectPath.resolve('build.gradle')

        Path dummy = TestConstants.testResource("ccv2-test-manifest.json")
        Files.copy(dummy, testProjectPath.resolve("manifest.json"))

        buildFile << """
            plugins {
                id 'sap.commerce.build'
                id 'sap.commerce.build.ccv2'
            }
        """

        runner = GradleRunner.create()
                .withProjectDir(testProjectPath.toFile())
        def gradleVersion = System.getenv("GRADLE_VERSION")
        if (gradleVersion) {
            runner.withGradleVersion(gradleVersion)
        }
        runner.withPluginClasspath()
    }

    def "validateManifest reuses configuration cache on second run"() {
        when: "first run stores the configuration cache"
        def firstResult = runner
                .withArguments('--configuration-cache', 'validateManifest')
                .build()

        then:
        firstResult.output.contains("Calculating task graph")
        !firstResult.output.contains("problems were found storing the configuration cache")

        when: "second run reuses the configuration cache"
        def secondResult = runner
                .withArguments('--configuration-cache', 'validateManifest')
                .build()

        then:
        secondResult.output.contains("Reusing configuration cache")
        !secondResult.output.contains("problems were found")
    }

    def "configuration cache is invalidated when manifest.json changes"() {
        given: "first run stores the configuration cache"
        runner.withArguments('--configuration-cache', 'validateManifest').build()

        when: "manifest.json content is replaced (triggers fileContents invalidation)"
        def manifestFile = testProjectPath.resolve("manifest.json")
        Path altManifest = TestConstants.testResource("manifest.2005.json")
        Files.copy(altManifest, manifestFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING)

        and: "second run re-evaluates with new manifest"
        def secondResult = runner
                .withArguments('--configuration-cache', 'validateManifest')
                .buildAndFail()  // manifest.2005.json may have validation errors, that's fine

        then: "the cache was not reused - task graph was recalculated"
        !secondResult.output.contains("Reusing configuration cache")
    }
}
