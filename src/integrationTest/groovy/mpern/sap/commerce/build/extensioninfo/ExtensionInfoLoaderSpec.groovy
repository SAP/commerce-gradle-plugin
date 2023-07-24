package mpern.sap.commerce.build.extensioninfo

import static mpern.sap.commerce.build.HybrisPlugin.HYBRIS_EXTENSION
import static mpern.sap.commerce.build.HybrisPlugin.HYBRIS_PLATFORM_CONFIGURATION
import static mpern.sap.commerce.build.HybrisPlugin.PLATFORM_NAME

import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

import spock.lang.Specification
import spock.lang.TempDir

import mpern.sap.commerce.build.ExtensionsTestUtils
import mpern.sap.commerce.build.HybrisPluginExtension
import mpern.sap.commerce.build.ProjectFolderTestUtils
import mpern.sap.commerce.build.TestUtils
import mpern.sap.commerce.build.util.Extension
import mpern.sap.commerce.build.util.ExtensionType
import mpern.sap.commerce.test.TestConstants

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
                .withProjectDir(TestConstants.testResource("dummy-custom-modules").toFile())
        project = projectBuilder.build()

        loader = new ExtensionInfoLoader(project)

        when:
        def extensions = loader.getExtensionsFromCustomFolder()

        then:
        extensions.size() == 2

        with(extensions.get("myextensionone" )) {
            it.extensionType == ExtensionType.CUSTOM
            it.directory.endsWith("custom/module/myextensionone")
            it.relativeLocation == "custom/module/myextensionone"
            it.requiredExtensions.empty
        }

        with(extensions.get("myextensiontwo")) {
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
        extensions.size() == 8

        with(extensions.get("apiregistryservices")) {
            it.extensionType == ExtensionType.SAP_MODULE
            it.directory.endsWith("modules/api-registry/apiregistryservices")
            it.relativeLocation == "modules/api-registry/apiregistryservices"
            it.requiredExtensions.empty
        }

        with(extensions.get("backoffice")) {
            it.extensionType == ExtensionType.SAP_MODULE
            it.directory.endsWith("modules/backoffice-framework/backoffice")
            it.relativeLocation == "modules/backoffice-framework/backoffice"
            it.requiredExtensions.empty
        }

        with(extensions.get("ybackoffice")) {
            it.extensionType == ExtensionType.SAP_MODULE
            it.directory.endsWith("modules/backoffice-framework/ybackoffice")
            it.relativeLocation == "modules/backoffice-framework/ybackoffice"
            it.requiredExtensions.size() == 1
            it.requiredExtensions.containsAll("backoffice")
        }

        with(extensions.get("basecommerce")) {
            it.extensionType == ExtensionType.SAP_MODULE
            it.directory.endsWith("modules/base-commerce/basecommerce")
            it.relativeLocation == "modules/base-commerce/basecommerce"
            it.requiredExtensions.size() == 1
            it.requiredExtensions.containsAll("apiregistryservices")
        }

        with(extensions.get("payment")) {
            it.extensionType == ExtensionType.SAP_MODULE
            it.directory.endsWith("modules/base-commerce/payment")
            it.relativeLocation == "modules/base-commerce/payment"
            it.requiredExtensions.size() == 2
            it.requiredExtensions.containsAll("apiregistryservices", "basecommerce")
        }

        with(extensions.get("yempty")) {
            it.extensionType == ExtensionType.SAP_MODULE
            it.directory.endsWith("modules/platform/yempty")
            it.relativeLocation == "modules/platform/yempty"
            it.requiredExtensions.empty
        }

        with(extensions.get("ruleengine")) {
            it.extensionType == ExtensionType.SAP_MODULE
            it.directory.endsWith("modules/rule-engine/ruleengine")
            it.relativeLocation == "modules/rule-engine/ruleengine"
            it.requiredExtensions.empty
        }

        with(extensions.get("searchservices")) {
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
            it.name == PLATFORM_NAME
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
        def allKnownExtensions = new HashMap<String, Extension>()

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
        HybrisPluginExtension extension = project.getExtensions().create(HYBRIS_EXTENSION, HybrisPluginExtension.class,
                project)
        ExtensionsTestUtils.ensureLocalExtensions(testProjectDir.toPath())

        loader = new ExtensionInfoLoader(project)

        and: "already loaded all extensions information"
        def allKnownExtensions = buildAllKnownExtensions()

        when:
        def allNeededExtensions = loader.loadAllNeededExtensions(allKnownExtensions)

        then:
        allNeededExtensions.size() == 8
        allNeededExtensions.any { it.key == PLATFORM_NAME }
        allNeededExtensions.any { it.key == "myextensionone" }
        allNeededExtensions.any { it.key == "myextensiontwo" }
        allNeededExtensions.any { it.key == "searchservices" }
        allNeededExtensions.any { it.key == "payment" }
        allNeededExtensions.any { it.key == "basecommerce" }
        allNeededExtensions.any { it.key == "apiregistryservices" }
        allNeededExtensions.any { it.key == "backoffice" }
    }

    def "correctly load the needed extensions with alwaysIncluded configuration present"() {
        given: "a Gradle project with localextensions.xml"
        def projectBuilder = ProjectBuilder.builder()
                .withName("test")
                .withProjectDir(testProjectDir)
        project = projectBuilder.build()
        HybrisPluginExtension extension = project.getExtensions().create(HYBRIS_EXTENSION, HybrisPluginExtension.class,
                project)
        extension.sparseBootstrap {
            enabled = true
            alwaysIncluded = ["yempty", "ybackoffice"]
        }

        ExtensionsTestUtils.ensureLocalExtensions(testProjectDir.toPath())

        loader = new ExtensionInfoLoader(project)

        and: "already loaded all extensions information"
        def allKnownExtensions = buildAllKnownExtensions()

        when:
        def allNeededExtensions = loader.loadAllNeededExtensions(allKnownExtensions)

        then:
        allNeededExtensions.size() == 10
        allNeededExtensions.any { it.key == PLATFORM_NAME }
        allNeededExtensions.any { it.key == "myextensionone" }
        allNeededExtensions.any { it.key == "myextensiontwo" }
        allNeededExtensions.any { it.key == "searchservices" }
        allNeededExtensions.any { it.key == "payment" }
        allNeededExtensions.any { it.key == "basecommerce" }
        allNeededExtensions.any { it.key == "apiregistryservices" }
        allNeededExtensions.any { it.key == "backoffice" }
        allNeededExtensions.any { it.key == "ybackoffice" }
        allNeededExtensions.any { it.key == "yempty" }
    }

    def "load platform in existing extensions when platform is present"() {
        given:
        def projectBuilder = ProjectBuilder.builder()
                .withName("test")
                .withProjectDir(testProjectDir)
        project = projectBuilder.build()

        loader = new ExtensionInfoLoader(project)

        and: "platform folder is present"
        def platformDir = testProjectDir.toPath().resolve("hybris/bin/platform/ext/core")
        Files.createDirectories(platformDir)

        when:
        def extensions = loader.loadAlreadyExistingExtensions()

        then:
        extensions.size() == 1
        extensions.containsKey(PLATFORM_NAME)
        with (extensions.get(PLATFORM_NAME)) {
            it.name == PLATFORM_NAME
            it.extensionType == ExtensionType.SAP_PLATFORM
            it.relativeLocation == "platform"
            it.requiredExtensions.isEmpty()
        }
    }

    def "do not load platform in existing extensions when platform only folder is present"() {
        given:
        def projectBuilder = ProjectBuilder.builder()
                .withName("test")
                .withProjectDir(testProjectDir)
        project = projectBuilder.build()

        loader = new ExtensionInfoLoader(project)

        and: "platform folder is present"
        def platformDir = testProjectDir.toPath().resolve("hybris/bin/platform")
        Files.createDirectories(platformDir)

        when:
        def extensions = loader.loadAlreadyExistingExtensions()

        then:
        extensions.isEmpty()
    }

    def "load existing extensions from project folder"() {
        given:
        def projectBuilder = ProjectBuilder.builder()
                .withName("test")
                .withProjectDir(testProjectDir)
        project = projectBuilder.build()

        loader = new ExtensionInfoLoader(project)

        ProjectFolderTestUtils.prepareProjectFolder(testProjectDir.toPath(), "dummy-platform-new-model")
        ProjectFolderTestUtils.prepareProjectFolder(testProjectDir.toPath(), "dummy-custom-modules")

        when:
        def extensions = loader.loadAlreadyExistingExtensions()

        then:

        println(extensions.size())
        println(extensions.sort { it.key }.collect { "${it.key} - ${it.value.relativeLocation}" }.join('\n'))

        extensions.size() == 11

        with(extensions.get("apiregistryservices")) {
            it.extensionType == ExtensionType.RUNTIME_INSTALLED
            it.directory.endsWith("modules/api-registry/apiregistryservices")
            it.relativeLocation == "modules/api-registry/apiregistryservices"
            it.requiredExtensions.empty
        }

        with(extensions.get("backoffice")) {
            it.extensionType == ExtensionType.RUNTIME_INSTALLED
            it.directory.endsWith("modules/backoffice-framework/backoffice")
            it.relativeLocation == "modules/backoffice-framework/backoffice"
            it.requiredExtensions.empty
        }

        with(extensions.get("ybackoffice")) {
            it.extensionType == ExtensionType.RUNTIME_INSTALLED
            it.directory.endsWith("modules/backoffice-framework/ybackoffice")
            it.relativeLocation == "modules/backoffice-framework/ybackoffice"
            it.requiredExtensions.size() == 1
            it.requiredExtensions.containsAll("backoffice")
        }

        with(extensions.get("basecommerce")) {
            it.extensionType == ExtensionType.RUNTIME_INSTALLED
            it.directory.endsWith("modules/base-commerce/basecommerce")
            it.relativeLocation == "modules/base-commerce/basecommerce"
            it.requiredExtensions.size() == 1
            it.requiredExtensions.containsAll("apiregistryservices")
        }

        with(extensions.get("payment")) {
            it.extensionType == ExtensionType.RUNTIME_INSTALLED
            it.directory.endsWith("modules/base-commerce/payment")
            it.relativeLocation == "modules/base-commerce/payment"
            it.requiredExtensions.size() == 2
            it.requiredExtensions.containsAll("apiregistryservices", "basecommerce")
        }

        with(extensions.get("yempty")) {
            it.extensionType == ExtensionType.RUNTIME_INSTALLED
            it.directory.endsWith("modules/platform/yempty")
            it.relativeLocation == "modules/platform/yempty"
            it.requiredExtensions.empty
        }

        with(extensions.get("ruleengine")) {
            it.extensionType == ExtensionType.RUNTIME_INSTALLED
            it.directory.endsWith("modules/rule-engine/ruleengine")
            it.relativeLocation == "modules/rule-engine/ruleengine"
            it.requiredExtensions.empty
        }

        with(extensions.get("searchservices")) {
            it.extensionType == ExtensionType.RUNTIME_INSTALLED
            it.directory.endsWith("modules/search-services/searchservices")
            it.relativeLocation == "modules/search-services/searchservices"
            it.requiredExtensions.empty
        }

        with (extensions.get(PLATFORM_NAME)) {
            it.name == PLATFORM_NAME
            it.extensionType == ExtensionType.SAP_PLATFORM
            it.relativeLocation == "platform"
            it.requiredExtensions.isEmpty()
        }
    }

    /**
     * Builds the known extension to match the dummy-custom-modules and dummy-platform-new-model test resources.
     *
     * @return the known extensions
     */
    private Map<String, Extension> buildAllKnownExtensions() {
        def allKnownExtensions = new HashMap<String, Extension>()

        allKnownExtensions.put(PLATFORM_NAME, new Extension(PLATFORM_NAME,
                FileSystems.default.getPath("hybris/bin/platform"),
                "hybris/bin/platform",
                ExtensionType.SAP_PLATFORM, Collections.emptyList()))

        allKnownExtensions.put("myextensionone", new Extension("myextensionone",
                FileSystems.default.getPath("hybris/bin/custom/module/myextensionone"),
                "custom/module/myextensionone",
                ExtensionType.CUSTOM, Collections.emptyList()))
        allKnownExtensions.put("myextensiontwo", new Extension("myextensiontwo",
                FileSystems.default.getPath("hybris/bin/custom/module/myextensiontwo"),
                "custom/module/myextensiontwo",
                ExtensionType.CUSTOM, Arrays.asList("searchservices")))

        allKnownExtensions.put("apiregistryservices", new Extension("apiregistryservices",
                FileSystems.default.getPath("hybriszip/extracted/hybris/bin/modules/api-registry/apiregistryservices"),
                "modules/api-registry/apiregistryservices",
                ExtensionType.SAP_MODULE, Collections.emptyList()))
        allKnownExtensions.put("backoffice", new Extension("backoffice",
                FileSystems.default.getPath("hybriszip/extracted/hybris/bin/modules/backoffice-framework/backoffice"),
                "modules/backoffice-framework/backoffice",
                ExtensionType.SAP_MODULE, Collections.emptyList()))
        allKnownExtensions.put("ybackoffice", new Extension("ybackoffice",
                FileSystems.default.getPath("hybriszip/extracted/hybris/bin/modules/backoffice-framework/ybackoffice"),
                "modules/backoffice-framework/ybackoffice",
                ExtensionType.SAP_MODULE, Arrays.asList("backoffice")))
        allKnownExtensions.put("basecommerce", new Extension("basecommerce",
                FileSystems.default.getPath("hybriszip/extracted/hybris/bin/modules/base-commerce/basecommerce"),
                "modules/base-commerce/basecommerce",
                ExtensionType.SAP_MODULE, Arrays.asList("apiregistryservices")))
        allKnownExtensions.put("payment", new Extension("payment",
                FileSystems.default.getPath("hybriszip/extracted/hybris/bin/modules/base-commerce/payment"),
                "modules/base-commerce/payment",
                ExtensionType.SAP_MODULE, Arrays.asList("apiregistryservices", "basecommerce")))
        allKnownExtensions.put("yempty", new Extension("yempty",
                FileSystems.default.getPath("hybriszip/extracted/hybris/bin/modules/platform/yempty"),
                "modules/platform/yempty",
                ExtensionType.SAP_MODULE, Collections.emptyList()))
        allKnownExtensions.put("ruleengine", new Extension("ruleengine",
                FileSystems.default.getPath("hybriszip/extracted/hybris/bin/modules/rule-engine/ruleengine"),
                "modules/rule-engine/ruleengine",
                ExtensionType.SAP_MODULE, Collections.emptyList()))
        allKnownExtensions.put("searchservices", new Extension("searchservices",
                FileSystems.default.getPath("hybriszip/extracted/hybris/bin/modules/search-services/searchservices"),
                "modules/search-services/searchservices",
                ExtensionType.SAP_MODULE, Collections.emptyList()))

        return allKnownExtensions
    }
}
