package mpern.sap.commerce.build.extensioninfo

import java.nio.file.Paths

import spock.lang.Specification

import mpern.sap.commerce.build.util.ExtensionType

class ExtensionXmlUtilTest extends Specification {

    static final ROOT_LOCATION = "/testExtensions/"

    def "file parsed correctly"() {
        given:
        def sourceFile = Paths.get(
                ExtensionXmlUtil.class.getResource("${ROOT_LOCATION}module/extension/extensioninfo.xml").toURI()).toFile()

        when:
        def extension = ExtensionXmlUtil.loadExtensionFromExtensioninfoXml(sourceFile, ROOT_LOCATION, ExtensionType.CUSTOM)

        then:
        with(extension) {
            name == "configurablebundlefacades"
            extensionType == ExtensionType.CUSTOM
            directory.endsWith("module/extension")
            relativeLocation == "module/extension"
            requiredExtensions.size() == 2
            requiredExtensions.containsAll("configurablebundleservices", "commercefacades")
        }
    }

    def "extension without name generates error"() {
        given:
        def sourceFile = Paths.get(
                ExtensionXmlUtil.class.getResource("${ROOT_LOCATION}module/brokenextension/extensioninfo.xml").toURI()).toFile()

        when:
        def extension = ExtensionXmlUtil.loadExtensionFromExtensioninfoXml(sourceFile, ROOT_LOCATION, ExtensionType.CUSTOM)

        then:
        ExtensionInfoException exception = thrown()
        exception.message.startsWith("Found extension without name in file ")
    }

    def "extension location not containing root location generates error"() {
        given:
        def sourceFile = Paths.get(
                ExtensionXmlUtil.class.getResource("${ROOT_LOCATION}module/extension/extensioninfo.xml").toURI()).toFile()

        when:
        def extension = ExtensionXmlUtil.loadExtensionFromExtensioninfoXml(sourceFile, "anotherRoot", ExtensionType.CUSTOM)

        then:
        ExtensionInfoException exception = thrown()
        exception.message.startsWith("Full location [")
    }
}
