package mpern.sap.commerce.build

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.gradle.testkit.runner.TaskOutcome.NO_SOURCE
import static org.gradle.testkit.runner.TaskOutcome.SKIPPED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class BootstrapTest extends Specification {
    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')

        def deps = testProjectDir.newFolder("dependencies").toPath()
        TestUtils.setupDependencies(deps)
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
    }


    def "bootstrap skipped when build.number correct"() {
        def version = 'TEST'
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
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("--stacktrace", 'bootstrapPlatform')
                .withPluginClasspath()
                .build()

        then: "the task is skipped"
        result.task(":cleanPlatformIfVersionChanged").outcome == SKIPPED
        result.task(":unpackPlatform").outcome == SKIPPED
    }

    def "boostrap extecuted when build.number wrong"() {
        def version = 'TEST'

        given:
        buildFile << """
            hybris {
                version = '$version'
            }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('bootstrapPlatform', '--stacktrace')
                .withPluginClasspath()
                .build()

        then:
        result.task(":bootstrapPlatform").outcome == SUCCESS
        result.task(":cleanPlatformIfVersionChanged").outcome == NO_SOURCE
        result.task(":unpackPlatform").outcome == SUCCESS
    }

    def "boostrap replaces platform if wrong version"() {
        def version = 'TEST'
        given:

        buildFile << """
            hybris {
                version = '$version'
            }
        """
        def buildFile = new File(testProjectDir.newFolder("hybris", "bin", "platform"), "build.number")
        buildFile << """
            version=WRONG_VERSION
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
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('bootstrapPlatform', '--stacktrace')
                .withPluginClasspath()
                .build()

        println(result.output)

        then:

        result.task(":bootstrapPlatform").outcome == SUCCESS

        customFile.exists()
        localProperties.exists()

        !dummyPlatformFile.exists()
        !dotFile.exists()
        !new File(testProjectDir.getRoot(), "hybris/bin/ext-accelerator").exists()

        buildFile.text.contains("version=TEST")
        new File(testProjectDir.getRoot(), "hybris/bin/ext-template/yaccelerator/src/dummy.java").exists()

    }

    def "boostrap sets up db drivers"() {
        def version = 'TEST'
        given:

        buildFile << """
            hybris {
                version = '$version'
            }
            dependencies {
                dbDriver 'some.database:jdbc:TEST'
            }
        """

        when: "running bootstrap task"

        def beforeBootstrap = new Date()
        beforeBootstrap.seconds -= 1

        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("--stacktrace", 'bootstrapPlatform')
                .withPluginClasspath()
                .build()

        def driverFile = new File(testProjectDir.getRoot(), "hybris/bin/platform/lib/dbdriver/jdbc-TEST.jar");
        def lastUpdate = new File(testProjectDir.getRoot(), "hybris/bin/platform/lib/dbdriver/.lastupdate");

        then:
        result.task(":bootstrapPlatform").outcome == SUCCESS

        driverFile.exists()
        lastUpdate.exists()
        lastUpdate.isFile()
        new Date(lastUpdate.lastModified()).after(beforeBootstrap)
    }

    def "cleanPlatform must delete all files as configured"() {

        given:
        def platformExtensionFolder = testProjectDir.newFolder("hybris", "bin", "ext-accelerator", "b2baddon", "src")
        def dummyPlatformFile = new File(platformExtensionFolder, "dummy.java")
        dummyPlatformFile.createNewFile()
        def dotFile = new File(testProjectDir.newFolder("hybris", "bin", "platform"), ".dotfile")
        dotFile.createNewFile()

        def customFile = new File(testProjectDir.newFolder("hybris", "bin", "custom", "customExtension", "src"), "dummy.java")
        customFile.createNewFile()

        def localProperties = new File(testProjectDir.newFolder("hybris", "config"), "local.properties")
        localProperties.createNewFile()

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments("--stacktrace", 'cleanPlatformIfVersionChanged')
                .withPluginClasspath()
                .build()

        then:
        customFile.exists()
        localProperties.exists()

        !dummyPlatformFile.exists()
        !dotFile.exists()
    }
}
