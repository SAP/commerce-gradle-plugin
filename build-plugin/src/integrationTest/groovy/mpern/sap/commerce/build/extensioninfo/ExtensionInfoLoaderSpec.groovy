package mpern.sap.commerce.build.extensioninfo

import static mpern.sap.commerce.build.HybrisPlugin.HYBRIS_EXTENSION
import static mpern.sap.commerce.build.HybrisPlugin.HYBRIS_PLATFORM_CONFIGURATION
import static mpern.sap.commerce.build.HybrisPlugin.PLATFORM_NAME

import java.nio.file.Files
import java.nio.file.Path

import org.gradle.api.Project
import org.gradle.api.file.FileCollection
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

    Project project
    HybrisPluginExtension extension
    ExtensionInfoLoader loader

    def setup() {
    }

    def initLoader() {
        extension = project.getExtensions().create(HYBRIS_EXTENSION, HybrisPluginExtension.class)
        FileCollection collection = project.configurations.create(HYBRIS_PLATFORM_CONFIGURATION)
        loader = project.getObjects().newInstance(ExtensionInfoLoader.class, extension, collection)
    }

    def "load extensions from custom folder"() {
        given: "a Hybris project with some custom modules"
        def projectBuilder = ProjectBuilder.builder()
                .withName("test")
                .withProjectDir(TestConstants.testResource("dummy-custom-modules").toFile())
        project = projectBuilder.build()

        initLoader()

        when:
        def extensions = loader.getExtensionsFromCustomFolder()

        then:
        extensions.size() == 2

        with(extensions.get("myextensionone" )) {
            it.extensionType == ExtensionType.CUSTOM
            it.relativeLocation == Path.of("custom/module/myextensionone")
            it.requiredExtensions.empty
        }

        with(extensions.get("myextensiontwo")) {
            it.extensionType == ExtensionType.CUSTOM
            it.relativeLocation == Path.of("custom/module/myextensiontwo")
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
        initLoader()
        project.dependencies.add(HYBRIS_PLATFORM_CONFIGURATION,
                project.files("${DEPENDENCIES_DIR}/hybris-commerce-suite-${HYBRIS_VERSION}.zip"))


        when:
        def extensions = loader.getExtensionsFromHybrisPlatformDependencies()

        then:
        extensions.size() == 8

        with(extensions.get("apiregistryservices")) {
            it.extensionType == ExtensionType.SAP_MODULE
            it.relativeLocation == Path.of("modules/api-registry/apiregistryservices")
            it.requiredExtensions.empty
        }

        with(extensions.get("backoffice")) {
            it.extensionType == ExtensionType.SAP_MODULE
            it.relativeLocation == Path.of("modules/backoffice-framework/backoffice")
            it.requiredExtensions.empty
        }

        with(extensions.get("ybackoffice")) {
            it.extensionType == ExtensionType.SAP_MODULE
            it.relativeLocation == Path.of("modules/backoffice-framework/ybackoffice")
            it.requiredExtensions.size() == 1
            it.requiredExtensions.containsAll("backoffice")
        }

        with(extensions.get("basecommerce")) {
            it.extensionType == ExtensionType.SAP_MODULE
            it.relativeLocation == Path.of("modules/base-commerce/basecommerce")
            it.requiredExtensions.size() == 1
            it.requiredExtensions.containsAll("apiregistryservices")
        }

        with(extensions.get("payment")) {
            it.extensionType == ExtensionType.SAP_MODULE
            it.relativeLocation == Path.of("modules/base-commerce/payment")
            it.requiredExtensions.size() == 2
            it.requiredExtensions.containsAll("apiregistryservices", "basecommerce")
        }

        with(extensions.get("yempty")) {
            it.extensionType == ExtensionType.SAP_MODULE
            it.relativeLocation == Path.of("modules/platform/yempty")
            it.requiredExtensions.empty
        }

        with(extensions.get("ruleengine")) {
            it.extensionType == ExtensionType.SAP_MODULE
            it.relativeLocation == Path.of("modules/rule-engine/ruleengine")
            it.requiredExtensions.empty
        }

        with(extensions.get("searchservices")) {
            it.extensionType == ExtensionType.SAP_MODULE
            it.relativeLocation == Path.of("modules/search-services/searchservices")
            it.requiredExtensions.empty
        }
    }

    def "correctly build the platform extension"() {
        given:
        def projectBuilder = ProjectBuilder.builder()
                .withName("test")
        project = projectBuilder.build()

        initLoader()

        when:
        def platformExt = loader.getPlatfromExtension()

        then:
        with(platformExt) {
            it.name == PLATFORM_NAME
            it.extensionType == ExtensionType.SAP_PLATFORM
            it.relativeLocation == Path.of("platform")
            it.requiredExtensions.empty
        }
    }

    def "raise error when loading the needed extensions and platform is missing"() {
        given:
        def projectBuilder = ProjectBuilder.builder()
                .withName("test")
        project = projectBuilder.build()

        initLoader()

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
        ExtensionsTestUtils.ensureLocalExtensions(testProjectDir.toPath())

        initLoader()

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
        initLoader()
        extension.sparseBootstrap {
            enabled = true
            alwaysIncluded = ["yempty", "ybackoffice"]
        }

        ExtensionsTestUtils.ensureLocalExtensions(testProjectDir.toPath())

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

        initLoader()

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
            it.relativeLocation == Path.of("platform")
            it.requiredExtensions.isEmpty()
        }
    }

    def "do not load platform in existing extensions when platform only folder is present"() {
        given:
        def projectBuilder = ProjectBuilder.builder()
                .withName("test")
                .withProjectDir(testProjectDir)
        project = projectBuilder.build()

        initLoader()

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

        initLoader()

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
            it.relativeLocation == Path.of("modules/api-registry/apiregistryservices")
            it.requiredExtensions.empty
        }

        with(extensions.get("backoffice")) {
            it.extensionType == ExtensionType.RUNTIME_INSTALLED
            it.relativeLocation == Path.of("modules/backoffice-framework/backoffice")
            it.requiredExtensions.empty
        }

        with(extensions.get("ybackoffice")) {
            it.extensionType == ExtensionType.RUNTIME_INSTALLED
            it.relativeLocation == Path.of("modules/backoffice-framework/ybackoffice")
            it.requiredExtensions.size() == 1
            it.requiredExtensions.containsAll("backoffice")
        }

        with(extensions.get("basecommerce")) {
            it.extensionType == ExtensionType.RUNTIME_INSTALLED
            it.relativeLocation == Path.of("modules/base-commerce/basecommerce")
            it.requiredExtensions.size() == 1
            it.requiredExtensions.containsAll("apiregistryservices")
        }

        with(extensions.get("payment")) {
            it.extensionType == ExtensionType.RUNTIME_INSTALLED
            it.relativeLocation == Path.of("modules/base-commerce/payment")
            it.requiredExtensions.size() == 2
            it.requiredExtensions.containsAll("apiregistryservices", "basecommerce")
        }

        with(extensions.get("yempty")) {
            it.extensionType == ExtensionType.RUNTIME_INSTALLED
            it.relativeLocation == Path.of("modules/platform/yempty")
            it.requiredExtensions.empty
        }

        with(extensions.get("ruleengine")) {
            it.extensionType == ExtensionType.RUNTIME_INSTALLED
            it.relativeLocation == Path.of("modules/rule-engine/ruleengine")
            it.requiredExtensions.empty
        }

        with(extensions.get("searchservices")) {
            it.extensionType == ExtensionType.RUNTIME_INSTALLED
            it.relativeLocation == Path.of("modules/search-services/searchservices")
            it.requiredExtensions.empty
        }

        with (extensions.get(PLATFORM_NAME)) {
            it.name == PLATFORM_NAME
            it.extensionType == ExtensionType.SAP_PLATFORM
            it.relativeLocation == Path.of("platform")
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
                Path.of("hybris/bin/platform"),
                ExtensionType.SAP_PLATFORM, Collections.emptyList()))

        allKnownExtensions.put("myextensionone", new Extension("myextensionone",
                Path.of("custom/module/myextensionone"),
                ExtensionType.CUSTOM, Collections.emptyList()))
        allKnownExtensions.put("myextensiontwo", new Extension("myextensiontwo",
                Path.of("custom/module/myextensiontwo"),
                ExtensionType.CUSTOM, Arrays.asList("searchservices")))

        allKnownExtensions.put("apiregistryservices", new Extension("apiregistryservices",
                Path.of("modules/api-registry/apiregistryservices"),
                ExtensionType.SAP_MODULE, Collections.emptyList()))
        allKnownExtensions.put("backoffice", new Extension("backoffice",
                Path.of("modules/backoffice-framework/backoffice"),
                ExtensionType.SAP_MODULE, Collections.emptyList()))
        allKnownExtensions.put("ybackoffice", new Extension("ybackoffice",
                Path.of("modules/backoffice-framework/ybackoffice"),
                ExtensionType.SAP_MODULE, Arrays.asList("backoffice")))
        allKnownExtensions.put("basecommerce", new Extension("basecommerce",
                Path.of("modules/base-commerce/basecommerce"),
                ExtensionType.SAP_MODULE, Arrays.asList("apiregistryservices")))
        allKnownExtensions.put("payment", new Extension("payment",
                Path.of("modules/base-commerce/payment"),
                ExtensionType.SAP_MODULE, Arrays.asList("apiregistryservices", "basecommerce")))
        allKnownExtensions.put("yempty", new Extension("yempty",
                Path.of("modules/platform/yempty"),
                ExtensionType.SAP_MODULE, Collections.emptyList()))
        allKnownExtensions.put("ruleengine", new Extension("ruleengine",
                Path.of("modules/rule-engine/ruleengine"),
                ExtensionType.SAP_MODULE, Collections.emptyList()))
        allKnownExtensions.put("searchservices", new Extension("searchservices",
                Path.of("modules/search-services/searchservices"),
                ExtensionType.SAP_MODULE, Collections.emptyList()))

        return allKnownExtensions
    }

    def "use correct platform/ext extension list for jdk21"() {

        given:
        def projectBuilder = ProjectBuilder.builder()
                .withName("test")
                .withProjectDir(testProjectDir)
        project = projectBuilder.build()
        initLoader()
        extension.getVersion().set("2211-jdk21.0")

        and: "a localextensions.xml requesting a platform/ext dependency of jdk21"
        def localextensions = testProjectDir.toPath().resolve(Path.of("hybris", "config", "localextensions.xml"))
        Files.createDirectories(localextensions.getParent())
        localextensions << """
        <hybrisconfig xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:noNamespaceSchemaLocation='../bin/platform/resources/schemas/extensions.xsd'>
          <extensions>
            <path dir='\${HYBRIS_BIN_DIR}' autoload='false' />
        
            <extension name='oauth2commons' />
          </extensions>
        </hybrisconfig>
        """.stripIndent()

        when:
        def extensions = loader.loadAllNeededExtensions(buildAllKnownExtensions())

        then:
        noExceptionThrown()
    }
    def "ExtensionInfoLoader detects if extension not available anymore in jdk21"() {

        given:
        def projectBuilder = ProjectBuilder.builder()
                .withName("test")
                .withProjectDir(testProjectDir)
        project = projectBuilder.build()
        initLoader()
        extension.getVersion().set("2211-jdk21.0")

        and: "a localextensions.xml requesting a platform/ext dependency of jdk17"
        def localextensions = testProjectDir.toPath().resolve(Path.of("hybris", "config", "localextensions.xml"))
        Files.createDirectories(localextensions.getParent())
        localextensions << """
        <hybrisconfig xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:noNamespaceSchemaLocation='../bin/platform/resources/schemas/extensions.xsd'>
          <extensions>
            <path dir='\${HYBRIS_BIN_DIR}' autoload='false' />
        
            <extension name='oauth2' />
          </extensions>
        </hybrisconfig>
        """.stripIndent()

        when:
        def extensions = loader.loadAllNeededExtensions(buildAllKnownExtensions())

        then:
        thrown(ExtensionInfoException.class)
    }
}
