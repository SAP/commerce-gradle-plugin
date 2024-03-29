package mpern.sap.commerce.ccv2

import static mpern.sap.commerce.build.TestUtils.ensureParents

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome

import spock.lang.Specification
import spock.lang.TempDir

import mpern.sap.commerce.build.TestUtils
import mpern.sap.commerce.test.TestConstants

class CCv2Tests extends Specification {

    @TempDir
    Path testProjectPath

    Path buildFile

    GradleRunner runner

    def setup() {
        buildFile = testProjectPath.resolve('build.gradle')

        def deps = testProjectPath.resolve("dependencies")
        Files.createDirectory(deps)

        TestUtils.generateDummyPlatform(deps, "1808.0")

        TestUtils.generateDummyPlatform(deps, "1905.1")

        TestUtils.generateDummyPlatform(deps, "2005.0")
        TestUtils.generateDummyIntegrationPack(deps, '2005.0')

        Path dummy =TestConstants.testResource("ccv2-test-manifest.json");
        Files.copy(dummy, testProjectPath.resolve("manifest.json"))

        buildFile << """
            plugins {
                id 'sap.commerce.build'
                id 'sap.commerce.build.ccv2'
            }
            repositories {
                flatDir {
                    dirs 'dependencies'
                }
            }
        """
        ensureParents(testProjectPath.resolve("hybris/bin/platform/build.number")) << """
            version=18.08
        """

        runner = GradleRunner.create()
                .withProjectDir(testProjectPath.toFile())
        def gradleVersion = System.getenv("GRADLE_VERSION")
        if (gradleVersion) {
            println "Using Gradle ${gradleVersion}"
            runner.withGradleVersion(gradleVersion)
        }
        runner.withPluginClasspath()
    }

    def "hybris.version is configured by manifest.json"() {
        given: "platform with different version"
        ensureParents(testProjectPath.resolve("hybris/bin/platform/build.number")) << """
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
        ensureParents(testProjectPath.resolve("hybris/bin/platform/build.number")) << """
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

    def "property files generated per aspect and persona"() {
        when: "running generateCloudProperties task"
        def result = runner
                .withArguments("generateCloudProperties", "--stacktrace")
                .build()

        def propFolder = testProjectPath.resolve("generated-configuration")

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

        def localExtensions = testProjectPath.resolve("generated-configuration").resolve("localextensions.xml").text

        then:
        localExtensions.contains("<extension name='modeltacceleratorservices' />")
        localExtensions.contains("<extension name='electronicsstore' />")
        localExtensions.contains("<extension name='privacyoverlayeraddon' />")
        localExtensions.contains("<extension name='yacceleratorstorefront' />")
        localExtensions.contains("<extension name='backoffice' />")
    }

    def "extensionPacks are automatically bootstrapped"() {
        given: "manifest with enabled integration-extension-pack pack"
        Path dummy = TestConstants.testResource("manifest.2005.json")
        Files.copy(dummy, testProjectPath.resolve("manifest.json"), StandardCopyOption.REPLACE_EXISTING)

        when: "running bootstrap task"
        runner.withArguments("bootstrapPlatform", "--stacktrace")
                .build()
        def cpiProject = testProjectPath.resolve("hybris/bin/modules/scpi/sapcpiproductexchange/project.properties")

        then:
        Files.exists(cpiProject)
        Files.isRegularFile(cpiProject)
    }

    def "cloudTests and cloudWebTests are always HybrisAntTask"() {
        given: "manifest with tests"
        testProjectPath.resolve("manifest.json").text = """\
        {
            "commerceSuiteVersion": "2011",
            "tests": {
                "annotations": ["UnitTests", "IntegrationTests"],
                "packages": ["com.demo."]
            }
        }
        """.stripIndent()

        buildFile << """
        assert tasks.getByName("cloudTests") instanceof mpern.sap.commerce.build.tasks.HybrisAntTask
        assert tasks.getByName("cloudWebTests") instanceof mpern.sap.commerce.build.tasks.HybrisAntTask
        """

        when: "running generateCloudProperties task"
        def result = runner.withArguments("--stacktrace").build()

        then:
        result != null
    }
}
