package mpern.sap.commerce.build

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

import static org.gradle.testkit.runner.TaskOutcome.*

class BootstrapTest extends Specification {
    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    String providedVersion = '2020.0'

    GradleRunner runner

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')

        buildFile << """
            plugins {
                id 'mpern.sap.commerce.build'
            }
            repositories {
                flatDir {
                    dirs 'dependencies'
                }
            }
        """
        def deps = testProjectDir.newFolder("dependencies").toPath()
        Path dbDriver = deps.resolve("jdbc-TEST.jar")
        Files.createFile(dbDriver)
        TestUtils.generateDummyPlatform(deps, providedVersion)

        runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
        def testVersion = System.getenv("GRADLE_VERSION")
        if (testVersion) {
            runner.withGradleVersion(testVersion)
        }
        runner.withPluginClasspath()
    }


    def "bootstrap skipped when build.number correct"() {
        def version = '1811.0'
        given: "correct version exists"
        buildFile << """
            hybris {
                version = '$version'
            }
        """
        new File(testProjectDir.newFolder("hybris", "bin", "platform"), "build.number") << """
            version=$version
        """

        when: "running bootstrap task"
        def result = runner
                .withArguments("--stacktrace", 'bootstrapPlatform')
                .build()

        then: "the task is skipped"
        result.task(":cleanPlatformIfVersionChanged").outcome == SKIPPED
        result.task(":unpackPlatform").outcome == SKIPPED
    }

    def "boostrap extecuted when no platform is there"() {
        given:
        buildFile << """
            hybris {
                version = '$providedVersion'
            }
        """

        when:
        def result = runner
                .withArguments('bootstrapPlatform', '--stacktrace')
                .build()

        then:
        result.task(":bootstrapPlatform").outcome == SUCCESS
        result.task(":cleanPlatformIfVersionChanged").outcome == NO_SOURCE
        result.task(":unpackPlatform").outcome == SUCCESS
    }

    def "boostrap replaces platform if wrong version"() {

        given:
        buildFile << """
            hybris {
                version = '$providedVersion'
            }
        """
        def buildFile = new File(testProjectDir.newFolder("hybris", "bin", "platform"), "build.number")
        buildFile << """
            version=18.08
        """

        def platformExtensionFolder = testProjectDir.newFolder("hybris", "bin", "ext-accelerator", "b2baddon", "src")
        def dummyPlatformFile = new File(platformExtensionFolder, "dummy.java")
        dummyPlatformFile.createNewFile()
        def dotFile = new File(platformExtensionFolder, ".dotfile")
        dotFile.createNewFile()

        def customFile = new File(testProjectDir.newFolder("hybris", "bin", "custom", "customExtension", "src"), "dummy.java")
        customFile.createNewFile()

        def localProperties = new File(testProjectDir.newFolder("hybris", "config"), "local.properties")
        localProperties.createNewFile()

        when: "running bootstrap task"
        def result = runner
                .withArguments('bootstrapPlatform', '--stacktrace')
                .build()

        println(result.output)

        then:

        result.task(":bootstrapPlatform").outcome == SUCCESS

        customFile.exists()
        localProperties.exists()

        !dummyPlatformFile.exists()
        !dotFile.exists()
        !new File(testProjectDir.getRoot(), "hybris/bin/ext-accelerator").exists()

        buildFile.text.contains("version=$providedVersion")
        new File(testProjectDir.getRoot(), "hybris/bin/ext-template/yaccelerator/src/dummy.java").exists()

    }

    def "boostrap sets up db drivers"() {
        given:

        buildFile << """
            hybris {
                version = '$providedVersion'
            }
            dependencies {
                dbDriver 'some.database:jdbc:TEST'
            }
        """

        when: "running bootstrap task"

        def beforeBootstrap = new Date()
        beforeBootstrap.seconds -= 1

        def result = runner
                .withArguments("--stacktrace", 'bootstrapPlatform')
                .build()

        def driverFile = new File(testProjectDir.getRoot(), "hybris/bin/platform/lib/dbdriver/jdbc-TEST.jar")
        def lastUpdate = new File(testProjectDir.getRoot(), "hybris/bin/platform/lib/dbdriver/.lastupdate")

        then:
        result.task(":bootstrapPlatform").outcome == SUCCESS

        driverFile.exists()
        lastUpdate.exists()
        lastUpdate.isFile()
        new Date(lastUpdate.lastModified()).after(beforeBootstrap)
    }

    def "cleanPlatform must delete all files as configured"() {

        given: "various platform files"
        def platformExtensionFolder = testProjectDir.newFolder("hybris", "bin", "ext-accelerator", "b2baddon", "src")
        def dummyPlatformFile = new File(platformExtensionFolder, "dummy.java")
        dummyPlatformFile.createNewFile()
        def dotFile = new File(testProjectDir.newFolder("hybris", "bin", "platform"), ".dotfile")
        dotFile.createNewFile()

        def customFile = new File(testProjectDir.newFolder("hybris", "bin", "custom", "customExtension", "src"), "dummy.java")
        customFile.createNewFile()

        def localProperties = new File(testProjectDir.newFolder("hybris", "config"), "local.properties")
        localProperties.createNewFile()

        when: "running the clean target"
        def result = runner.withArguments("--stacktrace", 'cleanPlatformIfVersionChanged')
                .withPluginClasspath()
                .build()

        then: "only platform files are deleted"
        customFile.exists()
        localProperties.exists()

        !dummyPlatformFile.exists()
        !dotFile.exists()
    }
}
