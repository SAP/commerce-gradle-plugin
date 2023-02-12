package mpern.sap.commerce.build

import static mpern.sap.commerce.build.TestUtils.ensureParents
import static org.gradle.testkit.runner.TaskOutcome.SKIPPED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.gradle.testkit.runner.GradleRunner

import spock.lang.Specification
import spock.lang.TempDir

class BootstrapSparseTest extends Specification {

    @TempDir
    Path testProjectDir

    Path buildFile

    String providedVersion = '2211.0'

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
        TestUtils.generateDummyPlatformNewModel(deps, providedVersion)

        runner = GradleRunner.create().withProjectDir(testProjectDir.toFile())
        def gradleVersion = System.getenv("GRADLE_VERSION")
        if (gradleVersion) {
            println "Using Gradle ${gradleVersion}"
            runner.withGradleVersion(gradleVersion)
        }
        runner.withPluginClasspath()
        //        runner.withDebug(true)
    }

    def "bootstrap skipped when build.number correct and no extensions change"() {
        def version = '2211.0'
        given: "correct version exists"
        buildFile << """
            hybris {
                version = '$version'
                sparseBootstrap {
                    enabled = true
                    alwaysIncluded = ['yempty', 'ybackoffice']
                }
            }
        """
        ensureParents(testProjectDir.resolve("hybris/bin/platform/build.number")) << """
            version=$version
        """

        when: "running bootstrap task"
        def result = runner
                .withArguments("--stacktrace", "--info", 'bootstrapPlatform')
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
                sparseBootstrap {
                    enabled = true
                    alwaysIncluded = ['yempty', 'ybackoffice']
                }
            }
        """

        ExtensionsTestUtils.generateExtension(
                testProjectDir.resolve("hybris/bin/custom/custom-module"), "myextensionone", Collections.emptyList())
        ExtensionsTestUtils.generateExtension(
                testProjectDir.resolve("hybris/bin/custom/custom-module"), "myextensiontwo", Collections.emptyList())
        ExtensionsTestUtils.ensureLocalExtensions(testProjectDir)

        when:
        def result = runner
                .withArguments('bootstrapPlatform', '--stacktrace')
                .build()

        then:
        result.task(":bootstrapPlatform").outcome == SUCCESS
        result.task(":unpackPlatform").outcome == SKIPPED
        result.task(":unpackPlatformSparse").outcome == SUCCESS

        Files.exists(testProjectDir.resolve("hybris/bin/custom/custom-module/myextensionone/extensioninfo.xml"))
        Files.exists(testProjectDir.resolve("hybris/bin/custom/custom-module/myextensiontwo/extensioninfo.xml"))
        Files.exists(testProjectDir.resolve("hybris/config/localextensions.xml"))
        Files.exists(testProjectDir.resolve("hybris/bin/modules/api-registry/apiregistryservices/extensioninfo.xml"))
        Files.exists(testProjectDir.resolve("hybris/bin/modules/backoffice-framework/backoffice/extensioninfo.xml"))
        Files.exists(testProjectDir.resolve("hybris/bin/modules/base-commerce/basecommerce/extensioninfo.xml"))
        Files.exists(testProjectDir.resolve("hybris/bin/modules/base-commerce/payment/extensioninfo.xml"))
        Files.exists(testProjectDir.resolve("hybris/bin/modules/search-services/searchservices/extensioninfo.xml"))
    }

    def "boostrap replaces platform if wrong version"() {

        given:
        buildFile << """
            hybris {
                version = '$providedVersion'
                sparseBootstrap {
                    enabled = true
                    alwaysIncluded = ['yempty', 'ybackoffice']
                }
            }
        """
        def buildFile = ensureParents(testProjectDir.resolve("hybris/bin/platform/build.number"))
        buildFile << """
            version=19.05
        """

        ExtensionsTestUtils.generateExtension(
                testProjectDir.resolve("hybris/bin/custom/custom-module", "myextensionone", Collections.emptyList()))

        def localProperties = ensureParents(testProjectDir.resolve("hybris/config/local.properties"))
        Files.createFile(localProperties)

        when: "running bootstrap task"
        def result = runner
                .withArguments('bootstrapPlatform', '--stacktrace')
                .build()

        println(result.output)

        then:

        result.task(":bootstrapPlatform").outcome == SUCCESS
        result.task(":cleanPlatformIfVersionChanged").outcome == SUCCESS
        result.task(":unpackPlatform").outcome == SKIPPED
        result.task(":unpackPlatformSparse").outcome == SUCCESS

        Files.exists(customFile)
        Files.exists(localProperties)

        !Files.exists(dummyPlatformFile)
        !Files.exists(dotFile)
        !Files.exists(testProjectDir.resolve(Paths.get("hybris/bin/ext-accelerator")))

        buildFile.text.contains("version=$providedVersion")
        Files.exists(testProjectDir.resolve(Paths.get("hybris/bin/ext-template/yaccelerator/src/dummy.java")))
    }

    def "boostrap executed when missing extensions"() {

        given:
        buildFile << """
            hybris {
                version = '$providedVersion'
                sparseBootstrap {
                    enabled = true
                    alwaysIncluded = ['yempty', 'ybackoffice']
                }
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
        result.task(":cleanPlatformIfVersionChanged").outcome == SKIPPED
        result.task(":unpackPlatform").outcome == SKIPPED
        result.task(":unpackPlatformSparse").outcome == SUCCESS

        Files.exists(customFile)
        Files.exists(localProperties)

        !Files.exists(dummyPlatformFile)
        !Files.exists(dotFile)
        !Files.exists(testProjectDir.resolve(Paths.get("hybris/bin/ext-accelerator")))

        buildFile.text.contains("version=$providedVersion")
        Files.exists(testProjectDir.resolve(Paths.get("hybris/bin/ext-template/yaccelerator/src/dummy.java")))
    }
}
