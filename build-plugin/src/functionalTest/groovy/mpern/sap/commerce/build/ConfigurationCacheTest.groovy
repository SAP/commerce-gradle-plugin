package mpern.sap.commerce.build

import java.nio.file.Files
import java.nio.file.Path

import org.gradle.testkit.runner.GradleRunner

import spock.lang.Specification
import spock.lang.TempDir

class ConfigurationCacheTest extends Specification {
    @TempDir
    Path testProjectDir

    GradleRunner runner

    def setup() {
        def buildFile = testProjectDir.resolve('build.gradle')
        buildFile << """
            plugins {
                id 'sap.commerce.build'
            }
        """

        runner = GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
        def gradleVersion = System.getenv("GRADLE_VERSION")
        if (gradleVersion) {
            runner.withGradleVersion(gradleVersion)
        }
        runner.withPluginClasspath()
    }

    def "cleanPlatform reuses configuration cache on second run"() {
        given: "a hybris/bin directory exists for the task to operate on"
        Files.createDirectories(testProjectDir.resolve("hybris/bin"))

        when: "first run stores the configuration cache"
        def firstResult = runner
                .withArguments('--configuration-cache', 'cleanPlatform')
                .build()

        then:
        firstResult.output.contains("Calculating task graph")
        !firstResult.output.contains("problems were found storing the configuration cache")

        when: "second run reuses the configuration cache"
        def secondResult = runner
                .withArguments('--configuration-cache', 'cleanPlatform')
                .build()

        then:
        secondResult.output.contains("Reusing configuration cache")
        !secondResult.output.contains("problems were found")
    }
}
