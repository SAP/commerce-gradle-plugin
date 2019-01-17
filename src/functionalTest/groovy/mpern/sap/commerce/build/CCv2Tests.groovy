package mpern.sap.commerce.build

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


class CCv2Tests extends Specification {
    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    String manifestVersion = "18.08.0"

    GradleRunner runner

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')

        def deps = testProjectDir.newFolder("dependencies").toPath()

        TestUtils.generateDummyPlatform(deps, "18.08.0")

        Path dummy = Paths.get(TestUtils.class.getResource("/test-manifest.json").toURI())
        Files.copy(dummy, testProjectDir.root.toPath().resolve("manifest.json"))

        buildFile << """
            plugins {
                id 'mpern.sap.commerce.build'
                id 'mpern.sap.commerce.build.ccv2'
            }
            repositories {
                flatDir {
                    dirs 'dependencies'
                }
            }
        """
        new File(testProjectDir.newFolder("hybris", "bin", "platform"), "build.number") << """
            version=18.08
        """

        runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
        def testVersion = System.getenv("GRADLE_VERSION")
        if (testVersion) {
            runner.withGradleVersion(testVersion)
        }
        runner.withPluginClasspath()
    }

    def "hybris.version is configured by manifest.json"() {
        given: "platform with different version"
        testProjectDir.root.toPath().resolve("hybris/bin/platform/build.number") << """
        version=2020.0
        """
        when: "running bootstrap task"
        def result = runner
                .withArguments("bootstrapPlatform", "--stacktrace")
                .build()

        then: "platform is cleaned and correct version unpacked"
        result.task(":cleanPlatformIfVersionChanged").outcome == TaskOutcome.SUCCESS
        result.task(":unpackPlatform").outcome == TaskOutcome.SUCCESS
    }

    def "configured version overrides manifest.json version"() {
        given: "platform with different version"
        testProjectDir.root.toPath().resolve("hybris/bin/platform/build.number") << """
        version=1811.0
        """
        buildFile << """
        hybris {
            version = '1811.0'
        }
        """

        when: "running bootstrap task"
        def result = runner
                .withArguments("bootstrapPlatform", "--stacktrace")
                .build()
        println(result.output)

        then: "platform is not modified"
        result.task(":cleanPlatformIfVersionChanged").outcome == TaskOutcome.SKIPPED
        result.task(":unpackPlatform").outcome == TaskOutcome.SKIPPED
    }

    def "property files generated per apsect and persona"() {
        when: "running generateCloudProperties task"
        def result = runner
                .withArguments("generateCloudProperties", "--stacktrace")
                .build()

        def propFolder = testProjectDir.root.toPath().resolve("generated-configuration")

        def commonProps = propFolder.resolve("common.properties")
        println result.output
        then:
        result.task(":generateCloudProperties").outcome == TaskOutcome.SUCCESS
        commonProps.text.contains("common.property=common.property.value")
    }

    def "generate localextensions based on manifest.json"() {
        when: "running generateCloudProperties task"
        def result = runner
                .withArguments("generateCloudLocalextensions", "--stacktrace")
                .build()

        def localExtensions = testProjectDir.root.toPath().resolve("generated-configuration").resolve("localextensions.xml").text

        then:
        localExtensions.contains("<extension name='modeltacceleratorservices' />")
        localExtensions.contains("<extension name='electronicsstore' />")
        localExtensions.contains("<extension name='privacyoverlayeraddon' />")
        localExtensions.contains("<extension name='yacceleratorstorefront' />")
        localExtensions.contains("<extension name='backoffice' />")
    }
}
