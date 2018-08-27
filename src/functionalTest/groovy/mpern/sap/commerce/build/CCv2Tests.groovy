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

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')

        def deps = testProjectDir.newFolder("dependencies").toPath()

        //version mismatch: manifest.json requires 18.08.0, but build.number file of 18.08 reports the version as 18.08 :(
        TestUtils.generateDummyPlatform(deps, "18.08")

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
            
            hybris {
            }
        """
        new File(testProjectDir.newFolder("hybris", "bin", "platform"), "build.number") << """
            version=18.08
        """
    }

    def "hybris.version is configured by manifest.json"() {
        testProjectDir.root.toPath().resolve("hybris/bin/platform/build.number") << """
        version=OTHER_VERSION
        """
        when: "running bootstrap task"
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments("bootstrapPlatform", "--stacktrace")
                .build()

        then: "the task is performed"
        result.task(":cleanPlatformIfVersionChanged").outcome == TaskOutcome.SUCCESS
        result.task(":unpackPlatform").outcome == TaskOutcome.SUCCESS
    }

    def "property files generated per apsect and persona"() {
        when: "running generateCloudProperties task"
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
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
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments("generateCloudLocalextensions", "--stacktrace")
                .build()

        def localExtensions = testProjectDir.root.toPath().resolve("generated-configuration").resolve("localextensions.xml").text
        println(localExtensions)
        then:
        localExtensions.contains("<extension name='modeltacceleratorservices' />")
        localExtensions.contains("<extension name='electronicsstore' />")
        localExtensions.contains("<extension name='privacyoverlayeraddon' />")
        localExtensions.contains("<extension name='yacceleratorstorefront' />")
        localExtensions.contains("<extension name='backoffice' />")
    }
}
