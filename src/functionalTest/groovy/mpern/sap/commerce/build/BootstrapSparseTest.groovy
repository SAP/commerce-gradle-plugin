package mpern.sap.commerce.build

import static mpern.sap.commerce.build.TestUtils.ensureParents
import static org.gradle.testkit.runner.TaskOutcome.SKIPPED
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

import java.nio.file.Files
import java.nio.file.Path

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

        ExtensionsTestUtils.ensureLocalExtensions(testProjectDir)

        runner = GradleRunner.create().withProjectDir(testProjectDir.toFile())
        def gradleVersion = System.getenv("GRADLE_VERSION")
        if (gradleVersion) {
            println "Using Gradle ${gradleVersion}"
            runner.withGradleVersion(gradleVersion)
        }
        runner.withPluginClasspath()
        //        runner.withDebug(true)
    }

    def "sparse bootstrap skipped when no missing extensions"() {
        given: "project folder contains all needed extensions"
        ProjectFolderTestUtils.prepareProjectFolder(testProjectDir, "dummy-platform-new-model")
        ProjectFolderTestUtils.prepareProjectFolder(testProjectDir, "dummy-custom-modules")

        and: "configured project with sparse bootstrap"
        buildFile << """
            hybris {
                version = '$providedVersion'
                sparseBootstrap {
                    enabled = true
                }
            }
        """
        ensureParents(testProjectDir.resolve("hybris/bin/platform/build.number")) << """
            version=$providedVersion
        """

        when: "running bootstrap task"
        def result = runner
                .withArguments("bootstrapPlatform", "--stacktrace")
                .build()

        println result.getOutput()

        then:
        result.task(":cleanPlatformIfVersionChanged").outcome == SKIPPED
        result.task(":unpackPlatform").outcome == SKIPPED
        result.task(":unpackPlatformSparse").outcome == SUCCESS

        result.output.contains("No missing SAP Commerce extensions, nothing to unpack")

        result.task(":bootstrapPlatform").outcome == SUCCESS
    }

    def "boostrap sparse extecuted when no platform is present"() {
        given: "project folder contains only custom extensions"
        ProjectFolderTestUtils.prepareProjectFolder(testProjectDir, "dummy-custom-modules")

        and: "configured project with sparse bootstrap"
        buildFile << """
            hybris {
                version = '$providedVersion'
                sparseBootstrap {
                    enabled = true
                }
            }
        """

        when:
        def result = runner
                .withArguments("bootstrapPlatform", "--stacktrace")
                .build()

        println result.getOutput()

        then:
        result.task(":bootstrapPlatform").outcome == SUCCESS
        result.task(":unpackPlatform").outcome == SKIPPED
        result.task(":unpackPlatformSparse").outcome == SUCCESS

        result.output.contains("Some needed SAP Commerce Suite extensions are missing, copying them")
        result.output.contains("Copying missing extensions from project dependency hybris-commerce-suite-2211.0.zip")
        result.output.contains("Copied missing extensions from project dependency hybris-commerce-suite-2211.0.zip")

        verifyProjectFilesPresent()
    }

    def "boostrap replaces platform if wrong version"() {
        given: "project folder contains all custom extensions"
        ProjectFolderTestUtils.prepareProjectFolder(testProjectDir, "dummy-custom-modules")

        and: "configured project with sparse bootstrap"
        buildFile << """
            hybris {
                version = '$providedVersion'
                sparseBootstrap {
                    enabled = true
                }
            }
        """
        and: "wrong platform version"
        ensureParents(testProjectDir.resolve("hybris/bin/platform/build.number")) << """
            version = 19.05
        """

        when: "running bootstrap task"
        def result = runner
                .withArguments("bootstrapPlatform", "--stacktrace")
                .build()

        println(result.output)

        then:

        result.task(":bootstrapPlatform").outcome == SUCCESS
        result.task(":cleanPlatformIfVersionChanged").outcome == SUCCESS
        result.task(":unpackPlatform").outcome == SKIPPED
        result.task(":unpackPlatformSparse").outcome == SUCCESS

        result.output.contains("Some needed SAP Commerce Suite extensions are missing, copying them")
        result.output.contains("Copying missing extensions from project dependency hybris-commerce-suite-2211.0.zip")
        result.output.contains("Copied missing extensions from project dependency hybris-commerce-suite-2211.0.zip")

        verifyProjectFilesPresent()
    }

    def "boostrap executed when missing extensions"() {
        given: "project folder misses some needed extensions"
        ProjectFolderTestUtils.prepareProjectFolder(testProjectDir, "dummy-platform-new-model")
        ProjectFolderTestUtils.prepareProjectFolder(testProjectDir, "dummy-custom-modules")
        ExtensionsTestUtils.removeExtension(testProjectDir, "modules/api-registry/apiregistryservices")
        ExtensionsTestUtils.removeExtension(testProjectDir, "modules/search-services/searchservices")
        // remove not needed dependencies
        ExtensionsTestUtils.removeExtension(testProjectDir, "modules/rule-engine/ruleengine")
        ExtensionsTestUtils.removeExtension(testProjectDir, "modules/backoffice-framework/ybackoffice")
        ExtensionsTestUtils.removeExtension(testProjectDir, "modules/platform/yempty")

        and: "configured project with sparse bootstrap"
        buildFile << """
            hybris {
                version = '$providedVersion'
                sparseBootstrap {
                    enabled = true
                }
            }
        """
        ensureParents(testProjectDir.resolve("hybris/bin/platform/build.number")) << """
            version=$providedVersion
        """

        when: "running bootstrap task"
        def result = runner
                .withArguments("bootstrapPlatform", "--stacktrace")
                .build()

        println(result.output)

        then:

        result.task(":bootstrapPlatform").outcome == SUCCESS
        result.task(":cleanPlatformIfVersionChanged").outcome == SKIPPED
        result.task(":unpackPlatform").outcome == SKIPPED
        result.task(":unpackPlatformSparse").outcome == SUCCESS

        result.output.contains("Some needed SAP Commerce Suite extensions are missing, copying them")
        result.output.contains("Copying missing extensions from project dependency hybris-commerce-suite-2211.0.zip")
        result.output.contains("Copied missing extensions from project dependency hybris-commerce-suite-2211.0.zip")

        verifyProjectFilesPresent()
    }

    def "boostrap sparse adds always included extensions"() {
        given: "project folder contains only custom extensions"
        ProjectFolderTestUtils.prepareProjectFolder(testProjectDir, "dummy-custom-modules")

        and: "configured project with sparse bootstrap and always included extensions"
        buildFile << """
            hybris {
                version = '$providedVersion'
                sparseBootstrap {
                    enabled = true
                    alwaysIncluded = [
                        "yempty",
                        "ybackoffice"
                    ]
                }
            }
        """

        when:
        def result = runner
                .withArguments("bootstrapPlatform", "--stacktrace")
                .build()

        println result.getOutput()

        then:
        result.task(":bootstrapPlatform").outcome == SUCCESS
        result.task(":unpackPlatform").outcome == SKIPPED
        result.task(":unpackPlatformSparse").outcome == SUCCESS

        result.output.contains("Some needed SAP Commerce Suite extensions are missing, copying them")
        result.output.contains("Copying missing extensions from project dependency hybris-commerce-suite-2211.0.zip")
        result.output.contains("Copied missing extensions from project dependency hybris-commerce-suite-2211.0.zip")

        verifyProjectFilesWithAlwaysIncludedPresent()
    }

    private void verifyProjectFilesPresent() {
        verifyOnlyNeededProjectFilesPresent()
        assert Files.notExists(testProjectDir.resolve("hybris/bin/modules/backoffice-framework/ybackoffice/extensioninfo.xml"))
        assert Files.notExists(testProjectDir.resolve("hybris/bin/modules/platform/yempty/extensioninfo.xml"))
    }

    private void verifyProjectFilesWithAlwaysIncludedPresent() {
        verifyOnlyNeededProjectFilesPresent()
        assert Files.exists(testProjectDir.resolve("hybris/bin/modules/backoffice-framework/ybackoffice/extensioninfo.xml"))
        assert Files.exists(testProjectDir.resolve("hybris/bin/modules/platform/yempty/extensioninfo.xml"))
    }

    private void verifyOnlyNeededProjectFilesPresent() {
        assert Files.exists(testProjectDir.resolve("hybris/bin/custom/module/myextensionone/extensioninfo.xml"))
        assert Files.exists(testProjectDir.resolve("hybris/bin/custom/module/myextensiontwo/extensioninfo.xml"))
        assert Files.exists(testProjectDir.resolve("hybris/config/localextensions.xml"))
        assert Files.exists(testProjectDir.resolve("hybris/bin/modules/api-registry/apiregistryservices/extensioninfo.xml"))
        assert Files.exists(testProjectDir.resolve("hybris/bin/modules/backoffice-framework/backoffice/extensioninfo.xml"))
        assert Files.exists(testProjectDir.resolve("hybris/bin/modules/base-commerce/basecommerce/extensioninfo.xml"))
        assert Files.exists(testProjectDir.resolve("hybris/bin/modules/base-commerce/payment/extensioninfo.xml"))
        assert Files.exists(testProjectDir.resolve("hybris/bin/modules/search-services/searchservices/extensioninfo.xml"))
        assert Files.notExists(testProjectDir.resolve("hybris/bin/modules/rule-engine/ruleengine/extensioninfo.xml"))
    }
}
