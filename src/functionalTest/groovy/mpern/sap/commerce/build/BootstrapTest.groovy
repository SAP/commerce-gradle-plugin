package mpern.sap.commerce.build

import static mpern.sap.commerce.build.TestUtils.ensureParents
import static org.gradle.testkit.runner.TaskOutcome.*

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant
import java.time.temporal.ChronoUnit

import org.gradle.testkit.runner.GradleRunner

import spock.lang.Specification
import spock.lang.TempDir

class BootstrapTest extends Specification {
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
        Path dbDriver = deps.resolve("jdbc-TEST.jar")
        Files.createFile(dbDriver)
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


    def "bootstrap skipped when build.number correct"() {
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

        when: "running bootstrap task"
        def result = runner
                .withArguments("--stacktrace", 'bootstrapPlatform')
                .build()

        then: "the task is skipped"
        result.task(":cleanPlatformIfVersionChanged").outcome == SKIPPED
        result.task(":unpackPlatform").outcome == SKIPPED
        result.task(":unpackPlatformSparse").outcome == SKIPPED
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
        result.task(":unpackPlatform").outcome == SUCCESS
        result.task(":unpackPlatformSparse").outcome == SKIPPED
    }

    def "boostrap replaces platform if wrong version"() {

        given:
        buildFile << """
            hybris {
                version = '$providedVersion'
            }
        """
        def buildFile = ensureParents(testProjectDir.resolve("hybris/bin/platform/build.number"))
        buildFile << """
            version=18.08
        """

        def platformExtensionFolder = testProjectDir.resolve("hybris/bin/ext-accelerator/b2baddon/src")
        Files.createDirectories(platformExtensionFolder)
        def dummyPlatformFile = platformExtensionFolder.resolve("dummy.java")
        Files.createFile(dummyPlatformFile)
        def dotFile = platformExtensionFolder.resolve(".dotfile")
        Files.createFile(dotFile)

        def customFile = ensureParents(testProjectDir.resolve("hybris/bin/custom/customExtension/src/dummy.java"))
        Files.createFile(customFile)

        def localProperties = ensureParents(testProjectDir.resolve("hybris/config/local.properties"))
        Files.createFile(localProperties)

        when: "running bootstrap task"
        def result = runner
                .withArguments('bootstrapPlatform', '--stacktrace')
                .build()

        println(result.output)

        then:

        result.task(":bootstrapPlatform").outcome == SUCCESS
        result.task(":unpackPlatformSparse").outcome == SKIPPED

        Files.exists(customFile)
        Files.exists(localProperties)

        !Files.exists(dummyPlatformFile)
        !Files.exists(dotFile)
        !Files.exists(testProjectDir.resolve(Paths.get("hybris/bin/ext-accelerator")))

        buildFile.text.contains("version=$providedVersion")
        Files.exists(testProjectDir.resolve(Paths.get( "hybris/bin/ext-template/yaccelerator/src/dummy.java")))
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

        /*
         we need to truncate because it seems like on mac,
         the lastModifiedDate does not contain milliseconds
         at least on GitHub Action MacOS runner.
         */
        def beforeBootstrap = Instant.now().truncatedTo(ChronoUnit.SECONDS)

        def result = runner
                .withArguments("--stacktrace", 'bootstrapPlatform')
                .build()

        def driverFile = testProjectDir.resolve("hybris/bin/platform/lib/dbdriver/jdbc-TEST.jar")
        def lastUpdate = testProjectDir.resolve("hybris/bin/platform/lib/dbdriver/.lastupdate")

        then:
        result.task(":bootstrapPlatform").outcome == SUCCESS
        result.task(":unpackPlatformSparse").outcome == SKIPPED

        Files.exists(driverFile)
        Files.exists(lastUpdate)
        Files.isRegularFile(lastUpdate)
        Files.getLastModifiedTime(lastUpdate).toInstant().truncatedTo(ChronoUnit.SECONDS) >= beforeBootstrap
    }

    def "cleanPlatform must delete all files as configured"() {

        given: "various platform files"
        def dummyPlatformFile = ensureParents(testProjectDir.resolve("hybris/bin/ext-accelerator/b2baddon/src/dummy.java"))
        Files.createFile(dummyPlatformFile)

        def dotFile = ensureParents(testProjectDir.resolve("hybris/bin/platform/.dotfile"))
        Files.createFile(dotFile)

        def customFile = ensureParents(testProjectDir.resolve("hybris/bin/custom/customExtension/src/dummy.java"))
        Files.createFile(customFile)

        def localProperties = ensureParents(testProjectDir.resolve("hybris/config/local.properties"))
        Files.createFile(localProperties)

        when: "running the clean target"
        def result = runner.withArguments("--stacktrace", 'cleanPlatformIfVersionChanged')
                .withPluginClasspath()
                .build()

        then: "only platform files are deleted"
        Files.exists(customFile)
        Files.exists(localProperties)

        !Files.exists(dummyPlatformFile)
        !Files.exists(dotFile)
    }
}
