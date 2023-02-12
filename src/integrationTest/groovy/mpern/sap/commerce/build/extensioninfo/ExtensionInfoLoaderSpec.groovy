package mpern.sap.commerce.build.extensioninfo

import mpern.sap.commerce.build.ExtensionsTestUtils
import mpern.sap.commerce.build.util.Extension

import java.nio.file.FileSystems

import static mpern.sap.commerce.build.HybrisPlugin.HYBRIS_PLATFORM_CONFIGURATION

import java.nio.file.Files
import java.nio.file.Paths

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

import spock.lang.Specification
import spock.lang.TempDir

import mpern.sap.commerce.build.TestUtils
import mpern.sap.commerce.build.util.ExtensionType

class ExtensionInfoLoaderSpec extends Specification {

    static final DEPENDENCIES_DIR = "dependencies"
    static final HYBRIS_VERSION = "2211.0"

    @TempDir
    File testProjectDir

    ExtensionInfoLoader loader

    Project project

    def setup() {
    }

    def "load extensions from custom folder"() {
        given: "a Hybris project with some custom modules"
        def projectBuilder = ProjectBuilder.builder()
                .withName("test")
                .withProjectDir(Paths.get(ExtensionInfoLoader.class.getResource("/dummy-custom-modules").toURI()).toFile())
        project = projectBuilder.build()

        loader = new ExtensionInfoLoader(project)

        when:
        def extensions = loader.getExtensionsFromCustomFolder()

        then:
        extensions.size() == 2

        with(extensions.find { it.name == "myextensionone" }) {
            it.extensionType == ExtensionType.CUSTOM
            it.directory.endsWith("custom/module/myextensionone")
            it.relativeLocation == "custom/module/myextensionone"
            it.requiredExtensions.empty
        }

        with(extensions.find { it.name == "myextensiontwo" }) {
            it.extensionType == ExtensionType.CUSTOM
            it.directory.endsWith("custom/module/myextensiontwo")
            it.relativeLocation == "custom/module/myextensiontwo"
            it.requiredExtensions.size() == 1
            it.requiredExtensions.containsAll("searchservices")
        }
    }

    def "load extensions from build hybrisPlatform named dependencies"() {
        given: "a hybris distribution zip"
        def depsDirPath = testProjectDir.toPath().resolve(DEPENDENCIES_DIR)
        Files.createDirectory(depsDirPath)
        TestUtils.generateDummyPlatformNewModel(depsDirPath, HYBRIS_VERSION)

        and: "a Gradle project with the hybris zip as dependency"
        def projectBuilder = ProjectBuilder.builder()
                .withName("test")
                .withProjectDir(testProjectDir)
        project = projectBuilder.build()
        project.configurations.create(HYBRIS_PLATFORM_CONFIGURATION)
        project.dependencies.add(HYBRIS_PLATFORM_CONFIGURATION,
                project.files("${DEPENDENCIES_DIR}/hybris-commerce-suite-${HYBRIS_VERSION}.zip"))

        loader = new ExtensionInfoLoader(project)

        when:
        def extensions = loader.getExtensionsFromHybrisPlatformDependencies()

        then:
        println(extensions.size())
        println(extensions.sort { it.name }.collect { "${it.name} - ${it.directory}" }.join('\n'))

        extensions.size() == 8

        with(extensions.find { it.name == "apiregistryservices" }) {
            it.extensionType == ExtensionType.SAP_MODULE
            it.directory.endsWith("modules/api-registry/apiregistryservices")
            it.relativeLocation == "modules/api-registry/apiregistryservices"
            it.requiredExtensions.empty
        }

        with(extensions.find { it.name == "backoffice" }) {
            it.extensionType == ExtensionType.SAP_MODULE
            it.directory.endsWith("modules/backoffice-framework/backoffice")
            it.relativeLocation == "modules/backoffice-framework/backoffice"
            it.requiredExtensions.empty
        }

        with(extensions.find { it.name == "ybackoffice" }) {
            it.extensionType == ExtensionType.SAP_MODULE
            it.directory.endsWith("modules/backoffice-framework/ybackoffice")
            it.relativeLocation == "modules/backoffice-framework/ybackoffice"
            it.requiredExtensions.size() == 1
            it.requiredExtensions.containsAll("backoffice")
        }

        with(extensions.find { it.name == "basecommerce" }) {
            it.extensionType == ExtensionType.SAP_MODULE
            it.directory.endsWith("modules/base-commerce/basecommerce")
            it.relativeLocation == "modules/base-commerce/basecommerce"
            it.requiredExtensions.size() == 1
            it.requiredExtensions.containsAll("apiregistryservices")
        }

        with(extensions.find { it.name == "payment" }) {
            it.extensionType == ExtensionType.SAP_MODULE
            it.directory.endsWith("modules/base-commerce/payment")
            it.relativeLocation == "modules/base-commerce/payment"
            it.requiredExtensions.size() == 2
            it.requiredExtensions.containsAll("apiregistryservices", "basecommerce")
        }

        with(extensions.find { it.name == "yempty" }) {
            it.extensionType == ExtensionType.SAP_MODULE
            it.directory.endsWith("modules/platform/yempty")
            it.relativeLocation == "modules/platform/yempty"
            it.requiredExtensions.empty
        }

        with(extensions.find { it.name == "ruleengine" }) {
            it.extensionType == ExtensionType.SAP_MODULE
            it.directory.endsWith("modules/rule-engine/ruleengine")
            it.relativeLocation == "modules/rule-engine/ruleengine"
            it.requiredExtensions.empty
        }

        with(extensions.find { it.name == "searchservices" }) {
            it.extensionType == ExtensionType.SAP_MODULE
            it.directory.endsWith("modules/search-services/searchservices")
            it.relativeLocation == "modules/search-services/searchservices"
            it.requiredExtensions.empty
        }
    }

    def "correctly build the platform extension"() {
        given:
        def projectBuilder = ProjectBuilder.builder()
                .withName("test")
        project = projectBuilder.build()

        loader = new ExtensionInfoLoader(project)

        when:
        def platformExt = loader.getPlatfromExtension()

        then:
        with(platformExt) {
            it.name == "platform"
            it.extensionType == ExtensionType.SAP_PLATFORM
            it.directory.endsWith("platform")
            it.relativeLocation == "platform"
            it.requiredExtensions.empty
        }
    }

    def "raise error when loading the needed extensions and platform is missing"() {
        given:
        def projectBuilder = ProjectBuilder.builder()
                .withName("test")
        project = projectBuilder.build()

        loader = new ExtensionInfoLoader(project)

        and: "already loaded all extensions information"
        def allKnownExtensions = new HashSet<Extension>()

        when:
        def allNeededExtensions = loader.loadAllNeededExtensions(allKnownExtensions)

        then:
        ExtensionInfoException e = thrown()
        e.message == "Platform extension not found"
    }

    def "correctly load the needed extensions"() {
        given: "a Gradle project with localextensions.xml"
        def projectBuilder = ProjectBuilder.builder()
                .withName("test")
                .withProjectDir(testProjectDir)
        project = projectBuilder.build()
        ExtensionsTestUtils.ensureLocalExtensions(testProjectDir.toPath())

        loader = new ExtensionInfoLoader(project)

        and: "already loaded all extensions information"
        def allKnownExtensions = buildAllKnownExtensions()

        when:
        def allNeededExtensions = loader.loadAllNeededExtensions(allKnownExtensions)

        then:
        allNeededExtensions.size() == 8
        allNeededExtensions.any { it.name == "platform" }
        allNeededExtensions.any { it.name == "myextensionone" }
        allNeededExtensions.any { it.name == "myextensiontwo" }
        allNeededExtensions.any { it.name == "searchservices" }
        allNeededExtensions.any { it.name == "payment" }
        allNeededExtensions.any { it.name == "basecommerce" }
        allNeededExtensions.any { it.name == "apiregistryservices" }
        allNeededExtensions.any { it.name == "backoffice" }
    }

    private Set<Extension> buildAllKnownExtensions() {
        def allKnownExtensions = new HashSet<Extension>()

        allKnownExtensions.add(new Extension("platform",
                FileSystems.default.getPath("hybris/bin/platform"),
                "hybris/bin/platform",
                ExtensionType.SAP_PLATFORM, Collections.emptyList()))

        allKnownExtensions.add(new Extension("myextensionone",
                FileSystems.default.getPath("hybris/bin/custom/module/myextensionone"),
                "custom/module/myextensionone",
                ExtensionType.CUSTOM, Collections.emptyList()))
        allKnownExtensions.add(new Extension("myextensiontwo",
                FileSystems.default.getPath("hybris/bin/custom/module/myextensiontwo"),
                "custom/module/myextensiontwo",
                ExtensionType.CUSTOM, Arrays.asList("searchservices")))

        allKnownExtensions.add(new Extension("apiregistryservices",
                FileSystems.default.getPath("hybriszip/extracted/hybris/bin/modules/api-registry/apiregistryservices"),
                "modules/api-registry/apiregistryservices",
                ExtensionType.SAP_MODULE, Collections.emptyList()))
        allKnownExtensions.add(new Extension("backoffice",
                FileSystems.default.getPath("hybriszip/extracted/hybris/bin/modules/backoffice-framework/backoffice"),
                "modules/backoffice-framework/backoffice",
                ExtensionType.SAP_MODULE, Collections.emptyList()))
        allKnownExtensions.add(new Extension("ybackoffice",
                FileSystems.default.getPath("hybriszip/extracted/hybris/bin/modules/backoffice-framework/ybackoffice"),
                "modules/backoffice-framework/ybackoffice",
                ExtensionType.SAP_MODULE, Arrays.asList("backoffice")))
        allKnownExtensions.add(new Extension("basecommerce",
                FileSystems.default.getPath("hybriszip/extracted/hybris/bin/modules/base-commerce/basecommerce"),
                "modules/base-commerce/basecommerce",
                ExtensionType.SAP_MODULE, Arrays.asList("apiregistryservices")))
        allKnownExtensions.add(new Extension("payment",
                FileSystems.default.getPath("hybriszip/extracted/hybris/bin/modules/base-commerce/payment"),
                "modules/base-commerce/payment",
                ExtensionType.SAP_MODULE, Arrays.asList("apiregistryservices", "basecommerce")))
        allKnownExtensions.add(new Extension("yempty",
                FileSystems.default.getPath("hybriszip/extracted/hybris/bin/modules/platform/yempty"),
                "modules/platform/yempty",
                ExtensionType.SAP_MODULE, Collections.emptyList()))
        allKnownExtensions.add(new Extension("ruleengine",
                FileSystems.default.getPath("hybriszip/extracted/hybris/bin/modules/rule-engine/ruleengine"),
                "modules/rule-engine/ruleengine",
                ExtensionType.SAP_MODULE, Collections.emptyList()))
        allKnownExtensions.add(new Extension("searchservices",
                FileSystems.default.getPath("hybriszip/extracted/hybris/bin/modules/search-services/searchservices"),
                "modules/search-services/searchservices",
                ExtensionType.SAP_MODULE, Collections.emptyList()))

        return allKnownExtensions
    }
}
