package mpern.sap.commerce.build

import static mpern.sap.commerce.build.TestUtils.ensureParents
import static org.gradle.testkit.runner.TaskOutcome.SKIPPED

import java.nio.file.Files
import java.nio.file.Path

import org.gradle.testkit.runner.GradleRunner

import spock.lang.Specification
import spock.lang.TempDir

class CleanupTest extends Specification {

    @TempDir
    Path testProjectDir

    Path buildFile

    String providedVersion = '2020.0'

    GradleRunner runner

    def setup() {
        buildFile = testProjectDir.resolve('build.gradle')

        buildFile << """
            plugins {
                id 'sap.commerce.build'
            }
            repositories {
                flatDir {
                    dirs 'dependencies'
                }
            }
        """

        def deps = testProjectDir.resolve("dependencies")
        Files.createDirectory(deps)
        TestUtils.generateDummyPlatform(deps, providedVersion)

        runner = GradleRunner.create()
                .withProjectDir(testProjectDir.toFile())
        def gradleVersion = System.getenv("GRADLE_VERSION")
        if (gradleVersion) {
            println "Using Gradle ${gradleVersion}"
            runner.withGradleVersion(gradleVersion)
        }
        runner.withPluginClasspath()
    }


    def "cleanPlatformIfVersionChanged skipped when same platfrom version"() {
        def version = '1811.0'

        given: "correct version exists"
        buildFile << """
            hybris {
                version = '$version'
            }
        """
        ensureParents(testProjectDir.resolve("hybris/bin/platform/build.number")) << """
            version=$version
        """

        when: "running cleanPlatformIfVersionChanged task"
        def result = runner
                .withArguments("--stacktrace", "-i", "cleanPlatformIfVersionChanged")
                .build()

        then: "the task is skipped"
        result.task(":cleanPlatformIfVersionChanged").outcome == SKIPPED
    }
}
